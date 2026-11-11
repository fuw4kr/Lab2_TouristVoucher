<?xml version="1.0" encoding="UTF-8"?>
<!--
    Changes the structure of the document: the flat list of vouchers is split
    into sub-lists by the type of the tour.

    before:                              after:
    <TouristVoucher>                     <TouristVoucher>
        <Voucher> rest                       <excursion vouchers="1">
        <Voucher> excursion                      <Voucher> ... </Voucher>
        <Voucher> rest                       </excursion>
    </TouristVoucher>                        <rest vouchers="2">
                                                 <Voucher> ... </Voucher>
                                                 <Voucher> ... </Voucher>
                                             </rest>
                                         </TouristVoucher>

    The grouping is done with a key (the Muenchian method), because XSLT 1.0,
    which is the version supported by the JDK, has no grouping instruction.
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="xml" encoding="UTF-8" indent="yes" standalone="yes"/>
    <xsl:strip-space elements="*"/>

    <xsl:key name="vouchers-of-type" match="Voucher" use="Type"/>

    <xsl:template match="/TouristVoucher">
        <TouristVoucher>
            <xsl:apply-templates mode="group"
                                 select="Voucher[count(. | key('vouchers-of-type', Type)[1]) = 1]">
                <xsl:sort select="Type"/>
            </xsl:apply-templates>
        </TouristVoucher>
    </xsl:template>

    <!-- is applied to the first voucher of every type and writes the whole group -->
    <xsl:template match="Voucher" mode="group">
        <xsl:variable name="group" select="key('vouchers-of-type', Type)"/>
        <xsl:element name="{Type}">
            <xsl:attribute name="vouchers">
                <xsl:value-of select="count($group)"/>
            </xsl:attribute>
            <xsl:apply-templates select="$group">
                <xsl:sort select="Country"/>
                <xsl:sort select="@id"/>
            </xsl:apply-templates>
        </xsl:element>
    </xsl:template>

    <!-- copies elements as they are, but without namespace declarations of the source root -->
    <xsl:template match="*">
        <xsl:element name="{name()}">
            <xsl:apply-templates select="@* | node()"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="@* | text()">
        <xsl:copy/>
    </xsl:template>
</xsl:stylesheet>
