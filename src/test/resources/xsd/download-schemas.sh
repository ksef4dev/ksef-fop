#!/usr/bin/env bash
#
# Downloads the KSeF FA schemas and their shared type schemas over HTTPS, then runs
# normalize-schema.xsl over each: caps maxOccurs > 100 to "unbounded" (keeps the content
# model under the IDE maxOccur limit) and rewrites import URLs to local filenames so the
# set is self-contained (no XML catalog / network).
#
# Trimmed for the local test fixtures only - real invoices may have more occurrences.
# Re-run to refresh.
set -euo pipefail

cd "$(dirname "$0")"

# Shared type schemas imported (transitively) by every FA schema.
DEFS_BASE="https://crd.gov.pl/xml/schematy/dziedzinowe/mf/2022/01/05/eD/DefinicjeTypy"
DEPS=(
  "StrukturyDanych_v10-0E.xsd"
  "ElementarneTypyDanych_v10-0E.xsd"
)

# Main FA schemas, served as <wzor>/<path>/schemat.xsd. Each entry is
# "<wzor-path>|<local-filename>".
WZOR_BASE="https://crd.gov.pl/wzor"
FA_SCHEMAS=(
  "2025/06/25/13775|Schemat_FA_VAT(3)_v1-0E.xsd"
  "2023/06/29/12648|Schemat_FA_VAT(2)_v1-0E.xsd"
  "2026/03/06/14189|Schemat_FA_RR(1)_v1-1E.xsd"
)

echo "Downloading shared type schemas from $DEFS_BASE ..."
for f in "${DEPS[@]}"; do
  echo "  - $f"
  curl -fsSL --max-time 60 "$DEFS_BASE/$f" -o "$f"
done

echo "Downloading FA schemas from $WZOR_BASE ..."
for entry in "${FA_SCHEMAS[@]}"; do
  path="${entry%%|*}"
  file="${entry##*|}"
  echo "  - $file  (<- $path/schemat.xsd)"
  curl -fsSL --max-time 60 "$WZOR_BASE/$path/schemat.xsd" -o "$file"
done

echo "Normalising schemas for local validation (normalize-schema.xsl) ..."
for entry in "${DEPS[@]}" "${FA_SCHEMAS[@]}"; do
  f="${entry##*|}"
  xsltproc normalize-schema.xsl "$f" > "$f.tmp"
  mv "$f.tmp" "$f"
done

echo "Done. Local schemas ready in $(pwd)."
