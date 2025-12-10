# Copyright 2025 Haui.HIT - H2K
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.



import argparse

import os

import sys

from pathlib import Path

from datetime import datetime, timezone



HEADER_TEMPLATES = {

    "c_like": """/*

 * Copyright {year} {owner}

 *

 * Licensed under the Apache License, Version 2.0 (the "License");

 * you may not use this file except in compliance with the License.

 * You may obtain a copy of the License at

 *

 *     http://www.apache.org/licenses/LICENSE-2.0

 *

 * Unless required by applicable law or agreed to in writing, software

 * distributed under the License is distributed on an "AS IS" BASIS,

 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

 * See the License for the specific language governing permissions and

 * limitations under the License.

 */

""",

    "ts_js": """/** 

 * Copyright {year} {owner}

 *

 * Licensed under the Apache License, Version 2.0 (the "License");

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software

 * distributed under the License is distributed on an "AS IS" BASIS,

 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

 */

""",

    "css": """/*!

 * Copyright {year} {owner}

 * Licensed under the Apache License, Version 2.0

 * http://www.apache.org/licenses/LICENSE-2.0

 */

""",

    "xml_html": """<!--

  Copyright {year} {owner}

  Licensed under the Apache License, Version 2.0

  http://www.apache.org/licenses/LICENSE-2.0

-->

""",

    "yaml": """# Copyright {year} {owner}

# Licensed under the Apache License, Version 2.0

# http://www.apache.org/licenses/LICENSE-2.0

""",

    "md": """<!--

  Copyright {year} {owner}

  Licensed under the Apache License, Version 2.0

  http://www.apache.org/licenses/LICENSE-2.0

-->

""",

    "sh": """# Copyright {year} {owner}

# Licensed under the Apache License, Version 2.0

# http://www.apache.org/licenses/LICENSE-2.0

"""

}



EXT_TO_TEMPLATE = {

    # C-like

    ".java": "c_like",

    ".kt": "c_like",

    # TypeScript/JS

    ".ts": "ts_js",

    ".tsx": "ts_js",

    ".js": "ts_js",

    ".jsx": "ts_js",

    # CSS

    ".css": "css",

    ".scss": "css",

    # XML/HTML

    ".xml": "xml_html",

    ".html": "xml_html",

    # YAML

    ".yml": "yaml",

    ".yaml": "yaml",

    # Markdown

    ".md": "md",

    ".mdx": "md",

    # Shell

    ".sh": "sh",

    # Python

    ".py": "sh",

}



SKIP_DIRS = {"node_modules", "build", "dist", ".git", "target", "out", ".gradle", ".idea", ".vscode"}

SKIP_FILE_PATTERNS = (".min.js", ".min.css")

# Config files that should not have SPDX identifier (only comment header)
# These files have specific formats that don't support SPDX identifier at the top
SKIP_SPDX_FILES = {
    "docker-compose.yml", "docker-compose.yaml",
    "package.json", "package-lock.json",
    "pom.xml",
    "application.yml", "application.yaml",
    "tsconfig.json", "jsconfig.json",
    "nuxt.config.ts", "vite.config.ts", "webpack.config.js"
}



def has_header(text: str) -> bool:

    markers = [

        "Licensed under the Apache License, Version 2.0",

        "SPDX-License-Identifier: Apache-2.0",

    ]

    return any(m in text[:1000] for m in markers)



def insert_header(text: str, header: str, spdx: bool, filename: str = "") -> str:
    # Don't add SPDX identifier - user preference is to only use comment headers
    return f"{header}{text}"



def process_file(path: Path, year: str, owner: str, spdx: bool) -> bool:

    ext = path.suffix.lower()

    if ext not in EXT_TO_TEMPLATE:

        return False

    if path.name.endswith(SKIP_FILE_PATTERNS):

        return False



    try:

        content = path.read_text(encoding="utf-8")

    except Exception:

        return False



    if has_header(content):

        return False



    tmpl_key = EXT_TO_TEMPLATE[ext]

    header = HEADER_TEMPLATES[tmpl_key].format(year=year, owner=owner)



    new_content = insert_header(content, header, spdx=spdx, filename=path.name)

    path.write_text(new_content, encoding="utf-8")

    return True



def main():

    ap = argparse.ArgumentParser()

    ap.add_argument("--root", default=".", help="Project root to scan")

    ap.add_argument("--year", default=str(datetime.now(timezone.utc).year))

    ap.add_argument("--owner", required=True)

    ap.add_argument("--spdx", action="store_true")

    args = ap.parse_args()



    root = Path(args.root).resolve()

    changed = 0



    for dirpath, dirnames, filenames in os.walk(root):

        # prune skip dirs

        dirnames[:] = [d for d in dirnames if d not in SKIP_DIRS]

        for fn in filenames:

            p = Path(dirpath) / fn

            if process_file(p, args.year, args.owner, args.spdx):

                changed += 1



    print(f"Headers inserted/normalized on {changed} files.")



if __name__ == "__main__":

    sys.exit(main())

