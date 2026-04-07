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
</xsl:stylesheet>
