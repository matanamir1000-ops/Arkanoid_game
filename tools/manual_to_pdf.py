#!/usr/bin/env python3
"""Render Game_Manual.md into Game_Manual.pdf.

The manual is written in Hebrew, so the page is laid out right-to-left and the
font stack names Segoe UI Emoji explicitly -- without it the emoji fall back to
a monochrome outline font.

Conversion goes Markdown -> HTML -> PDF, where the second step is a headless
Chrome or Edge printing the page. A browser is used rather than a PDF library
because it is the only thing on a stock Windows machine that gets bidirectional
text and colour emoji right at the same time.

Only the Markdown constructs the manual actually uses are supported: headings,
tables, unordered lists, block quotes, horizontal rules, bold, inline code, and
raw HTML passed straight through.

Usage:  python tools/manual_to_pdf.py
"""

import html
import os
import re
import subprocess
import sys
import tempfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SOURCE = os.path.join(ROOT, "Game_Manual.md")
TARGET = os.path.join(ROOT, "Game_Manual.pdf")

BROWSERS = [
    r"C:\Program Files\Google\Chrome\Application\chrome.exe",
    r"C:\Program Files (x86)\Google\Chrome\Application\chrome.exe",
    r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe",
    r"C:\Program Files\Microsoft\Edge\Application\msedge.exe",
]

STYLE = """
@page { size: A4; margin: 12mm 10mm; }

:root {
  --bg:        #0d0f1a;
  --panel:     #161a2e;
  --neon-pink: #ff2d95;
  --neon-cyan: #00e5ff;
  --neon-lime: #7cff4f;
  --neon-amber:#ffb020;
  --text:      #e8ecff;
  --muted:     #9aa3c7;
}

* { box-sizing: border-box; }

body {
  direction: rtl;
  text-align: right;
  background: var(--bg);
  color: var(--text);
  font-family: "Segoe UI", "Arial", "Noto Sans Hebrew", sans-serif, "Segoe UI Emoji";
  font-size: 10.5pt;
  line-height: 1.65;
  margin: 0;
  /* The page margin alone leaves a line that starts hard against the right
     edge clipped by a pixel or two, which shows up on every paragraph opening
     with a bold run. */
  padding: 0 4mm;
  -webkit-print-color-adjust: exact;
  print-color-adjust: exact;
}

h1 {
  font-size: 25pt;
  text-align: center;
  color: var(--neon-pink);
  margin: 0 0 4mm;
  letter-spacing: 1px;
  text-shadow: 0 0 12px rgba(255, 45, 149, 0.55);
}

h2 {
  font-size: 16pt;
  color: var(--neon-cyan);
  border-bottom: 2px solid var(--neon-cyan);
  padding-bottom: 1.5mm;
  margin: 8mm 0 3mm;
  page-break-after: avoid;
}

h3 {
  font-size: 12.5pt;
  color: var(--neon-lime);
  margin: 5mm 0 2mm;
  page-break-after: avoid;
}

p { margin: 0 0 2.5mm; }

strong { color: var(--neon-amber); }

code {
  font-family: "Consolas", "Courier New", monospace;
  background: #000;
  color: var(--neon-lime);
  padding: 0.4mm 1.4mm;
  border-radius: 2px;
  font-size: 9.5pt;
  direction: ltr;
  display: inline-block;
}

hr {
  border: 0;
  height: 2px;
  margin: 6mm 0;
  background: linear-gradient(90deg, var(--neon-pink), var(--neon-cyan), var(--neon-lime));
}

blockquote {
  margin: 3mm 0;
  padding: 2.5mm 4mm;
  background: var(--panel);
  border-right: 4px solid var(--neon-amber);
  border-radius: 3px;
  color: var(--muted);
}

blockquote strong { color: var(--neon-amber); }

ul { margin: 0 0 3mm; padding-right: 6mm; }
li { margin-bottom: 1mm; }

table {
  width: 100%;
  border-collapse: collapse;
  margin: 3mm 0 5mm;
  font-size: 9.5pt;
  page-break-inside: avoid;
}

th {
  background: var(--neon-pink);
  color: #12001a;
  font-weight: 700;
  padding: 2mm 2.5mm;
  border: 1px solid #2b3155;
}

td {
  padding: 2mm 2.5mm;
  border: 1px solid #2b3155;
  background: var(--panel);
}

tr:nth-child(even) td { background: #1c2140; }
"""


def inline(text):
    """Bold, inline code and escaping, in that order."""
    out = []
    for i, chunk in enumerate(text.split("`")):
        if i % 2 == 1:
            out.append("<code>" + html.escape(chunk) + "</code>")
        else:
            escaped = html.escape(chunk)
            out.append(re.sub(r"\*\*(.+?)\*\*", r"<strong>\1</strong>", escaped))
    return "".join(out)


def table_row(line, cell_tag):
    cells = [c.strip() for c in line.strip().strip("|").split("|")]
    body = "".join("<%s>%s</%s>" % (cell_tag, inline(c), cell_tag) for c in cells)
    return "<tr>%s</tr>" % body


def is_separator(line):
    return bool(re.match(r"^\|[\s:|-]+\|$", line.strip()))


def convert(markdown):
    lines = markdown.split("\n")
    out = []
    index = 0
    total = len(lines)

    while index < total:
        line = lines[index]
        stripped = line.strip()

        if not stripped:
            index += 1
            continue

        # Raw HTML is passed through untouched.
        if stripped.startswith("<"):
            out.append(stripped)
            index += 1
            continue

        if re.match(r"^-{3,}$", stripped):
            out.append("<hr>")
            index += 1
            continue

        heading = re.match(r"^(#{1,3})\s+(.*)$", stripped)
        if heading:
            level = len(heading.group(1))
            out.append("<h%d>%s</h%d>" % (level, inline(heading.group(2)), level))
            index += 1
            continue

        # A table: a header row, a separator, then body rows.
        if stripped.startswith("|") and index + 1 < total and is_separator(lines[index + 1]):
            rows = [table_row(stripped, "th")]
            index += 2
            while index < total and lines[index].strip().startswith("|"):
                rows.append(table_row(lines[index], "td"))
                index += 1
            out.append("<table>%s</table>" % "".join(rows))
            continue

        if stripped.startswith(">"):
            quoted = []
            while index < total and lines[index].strip().startswith(">"):
                quoted.append(inline(lines[index].strip()[1:].strip()))
                index += 1
            out.append("<blockquote>%s</blockquote>" % "<br>".join(quoted))
            continue

        if stripped.startswith("- "):
            items = []
            while index < total and lines[index].strip().startswith("- "):
                items.append("<li>%s</li>" % inline(lines[index].strip()[2:]))
                index += 1
            out.append("<ul>%s</ul>" % "".join(items))
            continue

        paragraph = []
        while index < total and lines[index].strip() and not re.match(
                r"^\s*(#|-{3,}|\||>|- )", lines[index]) and not lines[index].strip().startswith("<"):
            paragraph.append(inline(lines[index].strip()))
            index += 1
        out.append("<p>%s</p>" % "<br>".join(paragraph))

    return "\n".join(out)


def find_browser():
    for path in BROWSERS:
        if os.path.exists(path):
            return path
    return None


def main():
    with open(SOURCE, encoding="utf-8") as handle:
        markdown = handle.read()

    page = (
        '<!doctype html><html lang="he" dir="rtl"><head><meta charset="utf-8">'
        "<title>Arkanoid Manual</title><style>%s</style></head><body>%s</body></html>"
        % (STYLE, convert(markdown))
    )

    browser = find_browser()
    if browser is None:
        sys.exit("No Chrome or Edge found; cannot render the PDF.")

    temp_dir = tempfile.mkdtemp(prefix="manual-")
    html_path = os.path.join(temp_dir, "manual.html")
    with open(html_path, "w", encoding="utf-8") as handle:
        handle.write(page)

    result = subprocess.run([
        browser,
        "--headless=new",
        "--disable-gpu",
        "--no-pdf-header-footer",
        "--run-all-compositor-stages-before-draw",
        "--virtual-time-budget=6000",
        "--print-to-pdf=" + TARGET,
        "file:///" + html_path.replace("\\", "/"),
    ], capture_output=True, text=True)

    if not os.path.exists(TARGET):
        sys.exit("Rendering failed:\n" + result.stderr)

    print("Wrote %s (%d bytes)" % (TARGET, os.path.getsize(TARGET)))


if __name__ == "__main__":
    main()
