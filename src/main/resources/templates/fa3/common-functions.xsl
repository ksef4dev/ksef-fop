<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:local="urn:local">
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
