<?xml version="1.0" encoding="UTF-8"?>
<!--
  Normalises a downloaded KSeF schema for local (offline / IDE) validation:
    1. Replaces maxOccurs > 100 with "unbounded" so the content model stays under the
       validator's maxOccur limit (IntelliJ counts the sum of maxOccurs across the tree).
    2. Rewrites absolute import/include URLs to the bare local filename, so the set
       resolves to its siblings with no catalog or network.
  Trimmed for the local test fixtures only - real invoices may have more occurrences.
  Applied by download-schemas.sh:  xsltproc normalize-schema.xsl <schema.xsd>
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" encoding="UTF-8" indent="yes"/>

    <!-- Identity: copy every node and attribute unchanged. -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- Replace oversized maxOccurs with "unbounded" (values <= 100 are left untouched). -->
    <xsl:template match="@maxOccurs[. &gt; 100]">
        <xsl:attribute name="maxOccurs">unbounded</xsl:attribute>
    </xsl:template>

    <!-- Localise absolute import/include URLs to the bare local filename. -->
    <xsl:template match="@schemaLocation[contains(., '://')]">
        <xsl:attribute name="schemaLocation">
            <xsl:call-template name="basename">
                <xsl:with-param name="path" select="."/>
            </xsl:call-template>
        </xsl:attribute>
    </xsl:template>

    <!-- Returns the part of $path after the last '/'. -->
    <xsl:template name="basename">
        <xsl:param name="path"/>
        <xsl:choose>
            <xsl:when test="contains($path, '/')">
                <xsl:call-template name="basename">
                    <xsl:with-param name="path" select="substring-after($path, '/')"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$path"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
