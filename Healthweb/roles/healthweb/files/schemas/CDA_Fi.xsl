<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- CDA R2 Tyylitiedosto, HL7 Finland 19.8.2008 -->
<!-- Muutoshistoria -->
<!-- 2008.08.19: Päivitetty muuttuneen Header-määrittelyn mukaiseksi. AE -->
<!-- 2006.09.25: Päivitetty muuttuneen Header-määrittelyn mukaiseksi. Lisätty kuvalinkin esittäminen (AVE) -->
<!-- 2004.12.12: Lisätty Sovittujen Header-elementtien muotoilu katseltaviksi. -->
<!-- 2004.12.12: Lisätty content-elementin attribuutin revised-käsittely. Eli lisäykset ja poistot merkitään.-->
<!-- 2004.09.28: Lisätty xsl:output lause, jossa sanotaan merkkivalikoimaksi ISO-8859-1. -->
<!-- Muista päivittää tyylitiedoston versio html-head tagin jälkeen -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:n1="urn:hl7-org:v3" xmlns:n2="urn:hl7-org:v3/meta/voc" xmlns:voc="urn:hl7-org:v3/voc" xmlns:hl7fi="urn:hl7finland" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
	<xsl:output method="html" indent="yes" version="4.01" encoding="ISO-8859-1" doctype-public="-//W3C//DTD HTML 4.01//EN"/>
	<!-- CDA document -->
	<xsl:template match="/">
		<xsl:apply-templates select="n1:ClinicalDocument"/>
	</xsl:template>
	<xsl:template match="n1:ClinicalDocument">
		<html>
			<head/>
			<style>
				<xsl:comment>				
					 .class-normal {  font-style: normal; font-weight: normal; }
					 .class-bold {  font-style: normal; font-weight: bold; }
					 .class-underline { font-style: normal; font-weight: normal; text-decoration: underline; }
					 .class-italics { font-style: italic; font-weight: normal; }
					 .class-emphasis {  font-style: normal; font-weight: normal; background-color:yellow; }
					 .class-delete {  font-style: normal; font-weight: normal; text-decoration:line-through; }
					 .class-insert {  font-style: normal; font-weight: normal; text-decoration:underline; }
					 .class-caption {  font-style: normal; font-weight: bold;  }
	          	</xsl:comment>
			</style>
			<xsl:comment>				
              HL7 Finland  R2 Tyylitiedosto: Open CDA_2006
            </xsl:comment>
			<body>
				<em class="class-caption">
					<span style="font-weight:bold; color:blue;">
						<h3>
							<xsl:text>CDA R2 tuloste (CDA_Fi_ehr.xsl 15.9.2008)</xsl:text>
						</h3>
					</span>
				</em>
				<xsl:apply-templates select="n1:recordTarget"/>
				<xsl:apply-templates select="n1:componentOf/n1:encompassingEncounter/n1:responsibleParty/n1:assignedEntity/n1:representedOrganization"/>
				<xsl:apply-templates select="n1:componentOf/n1:encompassingEncounter/n1:effectiveTime"/>
				<xsl:apply-templates select="n1:componentOf/n1:encompassingEncounter/n1:encounterParticipant"/>
				<xsl:apply-templates select="n1:documentationOf"/>
				<br/>
				<xsl:apply-templates select="n1:component/n1:structuredBody"/>
			</body>
		</html>
	</xsl:template>
	<!-- 13 recordTarget potilaan tiedot -->
	<xsl:template match="n1:recordTarget">
		<em class="class-caption">
			<xsl:for-each select="n1:patientRole/n1:id">
				<span style="font-weight:bold; color:blue;">
					<xsl:text>Potilaan henkilötunnus: </xsl:text>
				</span>
				<xsl:value-of select="@root"/>
				<xsl:text>.</xsl:text>
				<xsl:value-of select="@extension"/>
				<br/>
			</xsl:for-each>
			<span style="font-weight:bold; color:blue;">
				<xsl:text>Potilaan nimi: </xsl:text>
			</span>
			<xsl:value-of select="n1:patientRole/n1:patient/n1:name/n1:family"/>
			<xsl:text>, </xsl:text>
			<xsl:value-of select="n1:patientRole/n1:patient/n1:name/n1:given"/>
			<br/>
			<span style="font-weight:bold; color:blue;">
				<xsl:text>Potilaan syntymäaika: </xsl:text>
			</span>
			<xsl:call-template name="date">
				<xsl:with-param name="date" select="n1:patientRole/n1:patient/n1:birthTime/@value"/>
			</xsl:call-template>
			<br/>
			<span style="font-weight:bold; color:blue;">
				<xsl:text>Potilaan sukupuoli: </xsl:text>
			</span>
			<xsl:for-each select="n1:patientRole/n1:patient/n1:administrativeGenderCode">
				<xsl:value-of select="@code"/>,&#160;<xsl:value-of select="@displayName"/>&#160;-- koodisto:&#160; 
				<xsl:value-of select="@codeSystem"/>,&#160;<xsl:value-of select="@codeSystemName"/>
			</xsl:for-each>
			<br/>
			<br/>
		</em>
	</xsl:template>
	<!-- 23. Annettu palvelu -->
	<xsl:template match="n1:documentationOf">
		<em class="class-caption">
			<span style="font-weight:bold; color:blue;">
				<xsl:text>Annettu palvelu: </xsl:text>
			</span>
			<xsl:value-of select="n1:serviceEvent/n1:code/@code"/>,&#160;<xsl:value-of select="n1:serviceEvent/n1:code/@displayName"/>&#160;-- koodisto:&#160; 
				<xsl:value-of select="n1:serviceEvent/n1:code/@codeSystem"/>,&#160;<xsl:value-of select="n1:serviceEvent/n1:code/@codeSystemName"/>
			<xsl:text> ajalta: </xsl:text>
			<xsl:call-template name="date">
				<xsl:with-param name="date" select="n1:serviceEvent/n1:effectiveTime/n1:low/@value"/>
			</xsl:call-template>
			<xsl:text> - </xsl:text>
			<xsl:call-template name="date">
				<xsl:with-param name="date" select="n1:serviceEvent/n1:effectiveTime/n1:high/@value"/>
			</xsl:call-template>
			<br/>
		</em>
	</xsl:template>
	<!-- 26.1 componentOf Palvelutapahtumatunnus -->
	<xsl:template match="n1:componentOf/n1:encompassingEncounter/n1:id">
		<em class="class-caption">
			<span style="font-weight:bold; color:blue;">
				<h3>
					<xsl:text>Palvelutapahtuman tiedot</xsl:text>
				</h3>
				<xsl:text>2.2.26.1 Palvelutapahtumatunnus: </xsl:text>
			</span>
			<xsl:value-of select="@root"/>
			<xsl:choose>
				<xsl:when test="@extension">
					<xsl:text>.</xsl:text>
					<xsl:value-of select="@extension"/>
				</xsl:when>
			</xsl:choose>
			<br/>
		</em>
	</xsl:template>
	<!-- 26.2 componentOf Palvelutapahtumatunnus -->
	<xsl:template match="n1:componentOf/n1:encompassingEncounter/n1:code">
		<em class="class-caption">
			<span style="font-weight:bold; color:blue;">
				<xsl:text>2.2.26.2 Palvelutapahtumaluokka: </xsl:text>
			</span>
			<xsl:value-of select="@code"/>,&#160;<xsl:value-of select="@displayName"/>&#160;-- koodisto:&#160; 
			<xsl:value-of select="@codeSystem"/>,&#160;<xsl:value-of select="@codeSystemName"/>
			<br/>
		</em>
	</xsl:template>
	<!-- 26.3 componentOf Hoitoaika -->
	<xsl:template match="n1:componentOf/n1:encompassingEncounter/n1:effectiveTime">
		<em class="class-caption">
			<span style="font-weight:bold; color:blue;">
				<xsl:text>Palvelutapahtuman kokonaishoitoaika: </xsl:text>
			</span>
			<xsl:call-template name="date">
				<xsl:with-param name="date" select="n1:low/@value"/>
			</xsl:call-template>
			<xsl:text> - </xsl:text>
			<xsl:call-template name="date">
				<xsl:with-param name="date" select="n1:high/@value"/>
			</xsl:call-template>
			<br/>
		</em>
	</xsl:template>
	<!-- 26.4 componentOf Palvelunantaja -->
	<xsl:template match="n1:componentOf/n1:encompassingEncounter/n1:responsibleParty/n1:assignedEntity/n1:representedOrganization">
		<em class="class-caption">
			<span style="font-weight:bold; color:blue;">
				<xsl:text>Palvelunantaja: </xsl:text>
			</span>
			<xsl:value-of select="n1:id/@root"/>
			<xsl:choose>
				<xsl:when test="n1:id/@extension">
					<xsl:text>.</xsl:text>
					<xsl:value-of select="n1:id/@extension"/>
				</xsl:when>
			</xsl:choose>
			<xsl:text>,&#160;</xsl:text>
			<xsl:value-of select="n1:name"/>
			<br/>
		</em>
	</xsl:template>
	<!-- 26.5 componentOf Palvelutapahtumatunnus -->
	<xsl:template match="n1:componentOf/n1:encompassingEncounter/n1:encounterParticipant">
		<em class="class-caption">
			<span style="font-weight:bold; color:blue;">
				<xsl:text>Palveluyksikön hoitoaika: </xsl:text>
			</span>
			<xsl:call-template name="date">
				<xsl:with-param name="date" select="n1:time/n1:low/@value"/>
			</xsl:call-template>
			<xsl:text> - </xsl:text>
			<xsl:call-template name="date">
				<xsl:with-param name="date" select="n1:time/n1:high/@value"/>
			</xsl:call-template>
			<span style="font-weight:bold; color:blue;">
				<xsl:text> palveluyksikkö: </xsl:text>
			</span>
			<xsl:value-of select="n1:assignedEntity/n1:representedOrganization/n1:name"/>
			<br/>
		</em>
	</xsl:template>
	<!-- StructuredBody -->
	<xsl:template match="n1:component/n1:structuredBody">
		<xsl:apply-templates select="n1:component/n1:section"/>
	</xsl:template>
	<!-- Component/Section -->
	<xsl:template match="n1:component/n1:section">
		<!-- siirrä objektin/merkinnän osoite html:n 15.9.2008  Antero Ensio-->
		<xsl:element name="a">
			<xsl:attribute name="name"><xsl:value-of select="@ID"/></xsl:attribute>
			<xsl:text/>
		</xsl:element>
		<xsl:apply-templates select="n1:title"/>
		<ul>
			<xsl:apply-templates select="n1:text"/>
			<xsl:apply-templates select="n1:component/n1:section"/>
		</ul>
	</xsl:template>
	<!--   Title  -->
	<xsl:template match="n1:title">
		<span style="font-weight:bold; color:green;">
			<xsl:value-of select="."/>
		</span>
	</xsl:template>
	<!--   Text   -->
	<xsl:template match="n1:text">
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="n1:br">
		<br/>
	</xsl:template>
	<!--   paragraph  -->
	<xsl:template match="n1:paragraph">
		<xsl:apply-templates/>
		<br/>
	</xsl:template>
	<!--   content  -->
	<xsl:template match="n1:content">
		<xsl:choose>
			<xsl:when test='@revised="delete"'>
				<em class="class-delete">
					<xsl:apply-templates/>
				</em>
			</xsl:when>
			<xsl:when test='@revised="insert"'>
				<em class="class-insert">
					<xsl:apply-templates/>
				</em>
			</xsl:when>
			<xsl:when test='@revised="Bold"'>
				<em class="class-bold">
					<xsl:apply-templates/>
				</em>
			</xsl:when>
			<xsl:when test='@revised="Underline"'>
				<em class="class-underline">
					<xsl:apply-templates/>
				</em>
			</xsl:when>
			<xsl:when test='@revised="Italics"'>
				<em class="class-italics">
					<xsl:apply-templates/>
				</em>
			</xsl:when>
			<xsl:when test='@revised="Emphasis"'>
				<em class="class-emphasis">
					<xsl:apply-templates/>
				</em>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!--   list  -->
	<xsl:template match="n1:list">
		<!--   listan otsikko  -->
		<xsl:if test="n1:caption">
			<span style="font-weight:bold; ">
				<xsl:apply-templates select="n1:caption"/>
			</span>
		</xsl:if>
		<!-- Jokainen listan alkio -->
		<xsl:for-each select="n1:item">
			<li>
				<!-- Lista-alkion elementti-->
				<xsl:apply-templates/>
			</li>
		</xsl:for-each>
	</xsl:template>
	<!--   caption  -->
	<xsl:template match="n1:caption">
		<xsl:apply-templates/>
		<xsl:text>: </xsl:text>
	</xsl:template>
	<!--      Tables
	 Koko alipuu kopioidaan sellaisenaan.
	 CAPTION elementin lapset käsitellään mahdollisilla muilla templateilla
  -->
	<xsl:template match="n1:table|n1:table/n1:caption|n1:thead|n1:tfoot|n1:tbody|n1:colgroup|n1:col|n1:tr|n1:th|n1:td">
		<xsl:copy>
			<xsl:apply-templates select="*|@*|text()"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="n1:table/@*|n1:thead/@*|n1:tfoot/@*|n1:tbody/@*|n1:colgroup/@*|n1:col/@*|n1:tr/@*|n1:th/@*|n1:td/@*">
		<xsl:copy>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="n1:table/n1:caption">
		<span style="font-weight:bold; ">
			<xsl:apply-templates/>
		</span>
	</xsl:template>
	<!-- Nimen tulostaminen -->
	<xsl:template name="getName">
		<xsl:apply-templates select="n1:name"/>
	</xsl:template>
	<!-- Päivämäärän ja kellonajan tai -välien muotoilu ulkoasuun pp.kk.vvvv klo hh:mm:ss -->
	<xsl:template name="date">
		<xsl:param name="date"/>
		<xsl:choose>
			<!-- päivämääräväli -->
			<xsl:when test="contains($date,'..')">
				<xsl:call-template name="datesingle">
					<xsl:with-param name="pdatesingle" select="substring-before ($date,'..')"/>
				</xsl:call-template>
				<xsl:text> - </xsl:text>
				<xsl:call-template name="datesingle">
					<xsl:with-param name="pdatesingle" select="substring-after ($date,'..')"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<!-- päivämäärä -->
				<xsl:call-template name="datesingle">
					<xsl:with-param name="pdatesingle" select="$date"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="datesingle">
		<xsl:param name="pdatesingle"/>
		<!-- pp-->
		<xsl:if test='substring ($pdatesingle, 7, 2)!=""'>
			<xsl:value-of select="format-number(substring ($pdatesingle,7, 2),0)"/>
			<xsl:text>.</xsl:text>
		</xsl:if>
		<!-- kk-->
		<xsl:if test='substring ($pdatesingle, 5, 2)!=""'>
			<xsl:value-of select="format-number(substring ($pdatesingle,5, 2),0)"/>
			<xsl:text>.</xsl:text>
		</xsl:if>
		<!-- vvvv-->
		<xsl:value-of select="substring ($pdatesingle, 1, 4)"/>
		<xsl:text/>
		<xsl:if test='substring ($pdatesingle, 9, 2)!=""'>
			<xsl:text> klo </xsl:text>
			<!-- hh-->
			<xsl:value-of select="substring ($pdatesingle,9, 2)"/>
		</xsl:if>
		<!-- mm -->
		<xsl:if test='substring ($pdatesingle, 11, 2)!=""'>
			<xsl:text>:</xsl:text>
			<xsl:value-of select="substring ($pdatesingle,11, 2)"/>
		</xsl:if>
		<!-- ss -->
		<xsl:if test='substring ($pdatesingle, 13, 2)!=""'>
			<xsl:text>:</xsl:text>
			<xsl:value-of select="substring ($pdatesingle,13, 2)"/>
		</xsl:if>
	</xsl:template>
	<!-- 	Stylecode processing   
	  Supports Bold, Underline and Italics display

-->
	<xsl:template match="//n1:*[@styleCode]">
		<xsl:if test="@styleCode='Bold'">
			<xsl:element name="b">
				<xsl:apply-templates/>
			</xsl:element>
		</xsl:if>
		<xsl:if test="@styleCode='Italics'">
			<xsl:element name="i">
				<xsl:apply-templates/>
			</xsl:element>
		</xsl:if>
		<xsl:if test="@styleCode='Underline'">
			<xsl:element name="u">
				<xsl:apply-templates/>
			</xsl:element>
		</xsl:if>
		<xsl:if test="contains(@styleCode,'Bold') and contains(@styleCode,'Italics') and not (contains(@styleCode, 'Underline'))">
			<xsl:element name="b">
				<xsl:element name="i">
					<xsl:apply-templates/>
				</xsl:element>
			</xsl:element>
		</xsl:if>
		<xsl:if test="contains(@styleCode,'Bold') and contains(@styleCode,'Underline') and not (contains(@styleCode, 'Italics'))">
			<xsl:element name="b">
				<xsl:element name="u">
					<xsl:apply-templates/>
				</xsl:element>
			</xsl:element>
		</xsl:if>
		<xsl:if test="contains(@styleCode,'Italics') and contains(@styleCode,'Underline') and not (contains(@styleCode, 'Bold'))">
			<xsl:element name="i">
				<xsl:element name="u">
					<xsl:apply-templates/>
				</xsl:element>
			</xsl:element>
		</xsl:if>
		<xsl:if test="contains(@styleCode,'Italics') and contains(@styleCode,'Underline') and contains(@styleCode, 'Bold')">
			<xsl:element name="b">
				<xsl:element name="i">
					<xsl:element name="u">
						<xsl:apply-templates/>
					</xsl:element>
				</xsl:element>
			</xsl:element>
		</xsl:if>
	</xsl:template>
	<!-- 	Superscript or Subscript   -->
	<xsl:template match="n1:sup">
		<xsl:element name="sup">
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="n1:sub">
		<xsl:element name="sub">
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	<!--   RenderMultiMedia 
renderMultiMedia.referencedObject -attribuutista poimitaan xml-ID viittaus vastaavaan rakenteisessa muodossa olevaan observationMedia-elementtiin ja laitetaan se apumuuttujaan imageRef
 -->
	<xsl:template match="n1:renderMultiMedia">
		<xsl:variable name="imageRef" select="@referencedObject"/>
		<!-- Haetaan xml-ID:n avulla vastaava observationMedia -->
		<xsl:if test="//n1:observationMedia[@ID=$imageRef]">
			<!-- Tulostetaan linkki kuvaan -->
			<!-- a-elementin href-attribuuttiin laitetaan kuvan osoite ja nimi, tässä tapauksessa esim. linkki -->
			<xsl:element name="a">
				<!-- Oletetaan, kaikki kuvat ovat saatavilla samasta palvelusta riippumatta OID root-arvosta -->
				<!-- Oletetaan, että kuva on tiedostossa, jonka nimi = extension-attribuutin arvo -->
				<xsl:attribute name="href"><xsl:text>http://193.185.85.122/csp/cdar2/displayImage.csp?acNumber=</xsl:text><xsl:value-of select="//n1:observationMedia[@ID=$imageRef]/n1:id/@extension"/></xsl:attribute>
				<!-- Jokin teksti, jota klikkaamalla selain aktivoi haun linkillä -->
				<xsl:text> Katso</xsl:text>
			</xsl:element>
		</xsl:if>
	</xsl:template>
	<!--   linkHtml 15.9.2008 Antero Ensio -->
	<xsl:template match="n1:linkHtml">
		<xsl:element name="a">
			<xsl:attribute name="href"><xsl:value-of select="@href"/></xsl:attribute>
			<!-- Jokin teksti, jota klikkaamalla selain aktivoi haun linkillä -->
			<xsl:value-of select="."/>
		</xsl:element>
	</xsl:template>
</xsl:stylesheet>
