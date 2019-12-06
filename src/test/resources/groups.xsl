<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" omit-xml-declaration="yes" indent="yes"/>
    <xsl:param name="project_name" />
    <xsl:template match="/">
        <table>
            <tr>
                <th>
                    <xsl:text>Name</xsl:text>
                </th>
                <th>
                    <xsl:text>Type</xsl:text>
                </th>
            </tr>
            <xsl:for-each select="/*[name()='Payload']/*[name()='Projects']/*[name()='Project' and @name=$project_name]/*[name()='Groups']/*[name()='Group']">
                <tr>
                    <td>
                        <xsl:value-of select="@name"/>
                    </td>
                    <td>
                        <xsl:value-of select="@type"/>
                    </td>
                </tr>
            </xsl:for-each>
        </table>
    </xsl:template>
</xsl:stylesheet>