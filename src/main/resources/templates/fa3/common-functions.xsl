<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:local="urn:local"
                xmlns:xs="http://www.w3.org/2001/XMLSchema">
    <xsl:function name="local:norm" as="xsd:string">
        <xsl:param name="value" as="item()?"/>
        <xsl:sequence select="normalize-space(string($value))"/>
    </xsl:function>

    <xsl:function name="local:softWrap" as="xsd:string">
        <xsl:param name="value" as="xsd:string?"/>
        <xsl:param name="chunkSize" as="xsd:integer"/>
        <xsl:variable name="v" select="string($value)"/>
        <xsl:sequence select="replace($v, concat('(.{', $chunkSize, '})'), concat('$1', '&#x200B;'))"/>
    </xsl:function>

    <!-- Dynamic table column width helpers (positions / order rows) -->
    <xsl:variable name="local:minProductNameColumnWidth" select="10"/>

    <xsl:function name="local:unitColumnWidthPercent" as="xs:double">
        <xsl:param name="unitMaxLen" as="xs:integer"/>
        <xsl:sequence select="
            if ($unitMaxLen gt 10) then 8
            else if ($unitMaxLen gt 6) then 7
            else if ($unitMaxLen gt 4) then 6
            else 5"/>
    </xsl:function>

    <xsl:function name="local:colPct" as="xs:string">
        <xsl:param name="width" as="xs:double"/>
        <xsl:sequence select="concat(format-number($width, '0.####'), '%')"/>
    </xsl:function>

    <!-- Sum of nominal column widths excluding the flexible product-name column -->
    <xsl:function name="local:positionFixedColumnsSum" as="xs:double">
        <xsl:param name="unitWidth" as="xs:double"/>
        <xsl:param name="showKwotaAkcyzy" as="xs:boolean"/>
        <xsl:param name="showP6A" as="xs:boolean"/>
        <xsl:param name="showP9A" as="xs:boolean"/>
        <xsl:param name="showP9B" as="xs:boolean"/>
        <xsl:param name="showP10" as="xs:boolean"/>
        <xsl:param name="showP12" as="xs:boolean"/>
        <xsl:param name="showP11" as="xs:boolean"/>
        <xsl:param name="showP11Vat" as="xs:boolean"/>
        <xsl:param name="showP11A" as="xs:boolean"/>
        <xsl:sequence select="
            4 + 8 + $unitWidth
            + (if ($showKwotaAkcyzy) then 8 else 0)
            + (if ($showP6A) then 9 else 0)
            + (if ($showP9A) then 10 else 0)
            + (if ($showP9B) then 10 else 0)
            + (if ($showP10) then 7 else 0)
            + (if ($showP12) then 8 else 0)
            + (if ($showP11) then 10 else 0)
            + (if ($showP11Vat) then 7 else 0)
            + (if ($showP11A) then 10 else 0)"/>
    </xsl:function>

    <!-- Leave 1% slack so borders/padding do not exceed the page width -->
    <xsl:variable name="local:positionTableWidthBudget" select="99"/>

    <xsl:function name="local:positionColumnScale" as="xs:double">
        <xsl:param name="fixedSum" as="xs:double"/>
        <xsl:param name="showProductName" as="xs:boolean"/>
        <xsl:variable name="budget" select="$local:positionTableWidthBudget - (if ($showProductName) then $local:minProductNameColumnWidth else 0)"/>
        <xsl:sequence select="if ($fixedSum gt $budget) then $budget div $fixedSum else 1"/>
    </xsl:function>

    <xsl:function name="local:positionNameColumnWidth" as="xs:double">
        <xsl:param name="fixedSum" as="xs:double"/>
        <xsl:param name="scale" as="xs:double"/>
        <xsl:param name="showProductName" as="xs:boolean"/>
        <xsl:sequence select="
            if (not($showProductName)) then 0
            else max(($local:minProductNameColumnWidth, $local:positionTableWidthBudget - ($fixedSum * $scale)))"/>
    </xsl:function>

    <xsl:function name="local:scaledCol" as="xs:string">
        <xsl:param name="nominal" as="xs:double"/>
        <xsl:param name="scale" as="xs:double"/>
        <xsl:sequence select="local:colPct($nominal * $scale)"/>
    </xsl:function>

    <xsl:function name="local:orderFixedColumnsSum" as="xs:double">
        <xsl:param name="showUU_IDZ" as="xs:boolean"/>
        <xsl:param name="showP9AZ" as="xs:boolean"/>
        <xsl:param name="showP11NettoZ" as="xs:boolean"/>
        <xsl:param name="showP11VatZ" as="xs:boolean"/>
        <xsl:sequence select="
            4 + 12 + 6
            + (if ($showUU_IDZ) then 14 else 0)
            + (if ($showP9AZ) then 12 else 0)
            + 8
            + (if ($showP11NettoZ) then 12 else 0)
            + (if ($showP11VatZ) then 10 else 0)"/>
    </xsl:function>

    <xsl:variable name="local:orderTableWidthBudget" select="100"/>

    <xsl:function name="local:orderColumnScale" as="xs:double">
        <xsl:param name="fixedSum" as="xs:double"/>
        <xsl:param name="showProductName" as="xs:boolean"/>
        <xsl:variable name="budget" select="$local:orderTableWidthBudget - (if ($showProductName) then $local:minProductNameColumnWidth else 0)"/>
        <xsl:sequence select="if ($fixedSum gt $budget) then $budget div $fixedSum else 1"/>
    </xsl:function>

    <xsl:function name="local:orderNameColumnWidth" as="xs:double">
        <xsl:param name="fixedSum" as="xs:double"/>
        <xsl:param name="scale" as="xs:double"/>
        <xsl:param name="showProductName" as="xs:boolean"/>
        <xsl:sequence select="
            if (not($showProductName)) then 0
            else max(($local:minProductNameColumnWidth, $local:orderTableWidthBudget - ($fixedSum * $scale)))"/>
    </xsl:function>

    <!-- Format used for formatting decimal numbers -->
    <xsl:decimal-format name="local:pl" decimal-separator="," grouping-separator=" "/>
    <xsl:function name="local:format-amount" as="xsd:string">
        <xsl:param name="value" as="item()?"/>
        <xsl:sequence select="format-number(number($value), '# ##0,00', 'local:pl')"/>
    </xsl:function>
    <xsl:function name="local:format-integer" as="xsd:string">
        <xsl:param name="value" as="item()?"/>
        <xsl:sequence select="format-number(number($value), '# ##0', 'local:pl')"/>
    </xsl:function>
    <xsl:function name="local:format-quantity" as="xsd:string">
        <xsl:param name="value" as="item()?"/>
        <xsl:sequence select="format-number(number($value), '# ##0,######', 'local:pl')"/>
    </xsl:function>
    <xsl:function name="local:format-unit-price" as="xsd:string">
        <xsl:param name="value" as="item()?"/>
        <xsl:sequence select="format-number(number($value), '# ##0,00######', 'local:pl')"/>
    </xsl:function>

    <!-- ISO dateTime (np. 2026-04-28T00:00:00Z) → 28.04.2026 00:00 -->
    <xsl:function name="local:format-data-czas" as="xsd:string">
        <xsl:param name="raw" as="xsd:string?"/>
        <xsl:variable name="rawn" select="normalize-space(string($raw))"/>
        <xsl:choose>
            <xsl:when test="$rawn = ''">
                <xsl:sequence select="''"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="datePart" select="if (contains($rawn, 'T')) then substring-before($rawn, 'T') else $rawn"/>
                <xsl:variable name="timePart" select="if (contains($rawn, 'T')) then substring-after($rawn, 'T') else ''"/>
                <xsl:variable name="dd"
                              select="if (string-length($datePart) ge 10) then substring($datePart, 9, 2) else ''"/>
                <xsl:variable name="mm"
                              select="if (string-length($datePart) ge 10) then substring($datePart, 6, 2) else ''"/>
                <xsl:variable name="yyyy"
                              select="if (string-length($datePart) ge 10) then substring($datePart, 1, 4) else ''"/>
                <xsl:variable name="timeClean" select="replace($timePart, 'Z$|([\+\-]\d{2}:\d{2})$', '')"/>
                <xsl:variable name="hm"
                              select="if ($timeClean != '') then replace($timeClean, '^(\d{2}:\d{2}).*', '$1') else ''"/>
                <xsl:variable name="hm2" select="if (matches($hm, '^\d{2}:\d{2}$')) then $hm else ''"/>
                <xsl:variable name="dateOut"
                              select="if ($dd != '' and $mm != '' and $yyyy != '') then concat($dd, '.', $mm, '.', $yyyy) else $datePart"/>
                <xsl:sequence select="if ($hm2 != '') then concat($dateOut, ' ', $hm2) else $dateOut"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>
</xsl:stylesheet>
