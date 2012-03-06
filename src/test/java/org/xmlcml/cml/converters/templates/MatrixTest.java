package org.xmlcml.cml.converters.templates;

import org.junit.Assert;
import org.junit.Test;
import org.xmlcml.cml.base.CMLUtil;
import org.xmlcml.cml.converters.templates.output.LineContainer;
import org.xmlcml.cml.converters.templates.output.MarkupContainer;
import org.xmlcml.cml.converters.templates.output.Template;
import org.xmlcml.cml.testutil.JumboTestUtils;

public class MatrixTest {

	@Test
	public void testMatrix0() {
		String templateS = 
				"<template>" +
				"  <matrix/>" +
				"</template>";
			MarkupContainer template = new Template(CMLUtil.parseXML(templateS));
			Assert.assertNotNull("template", template);
	}
	
	@Test
	public void testRow() {
		String templateS = 
				"<template>" +
				"  <matrix>" +
				"    <record id='r1' repeat='*'/>" +
				"  </matrix>" +
				"</template>";
			MarkupContainer template = new Template(CMLUtil.parseXML(templateS));
			Assert.assertNotNull("template", template);
	}
	
	@Test
	public void testFields() {
		String templateS = 
				"<template>" +
				"  <matrix>" +
				"    <record id='r1' repeat='*' matrixFields='header'/>" +
				"    <record id='r1' repeat='*' matrixFields='body'/>" +
				"    <record id='r1' repeat='*' matrixFields='left body right'/>" +
				"    <record id='r1' repeat='*' matrixFields='footer'/>" +
				"  </matrix>" +
				"</template>";
			MarkupContainer template = new Template(CMLUtil.parseXML(templateS));
			Assert.assertNotNull("template", template);
	}
	
	@Test
	public void testBadFields() {
		String templateS = 
				"<template>" +
				"  <matrix>" +
				"    <record id='r1' repeat='*' matrixFields='heder'/>" +
				"  </matrix>" +
				"</template>";
		try {
			MarkupContainer template = new Template(CMLUtil.parseXML(templateS));
			Assert.assertTrue("must throw exception", true);
		} catch (Exception e) {
			
		}
	}
	
	@Test
	public void testBody() {
		String templateS = 
				"<template>" +
				"  <matrix id='m1'>" +
				"    <record id='r1' repeat='*' matrixFields='body'>\\s*{2F,x:x}\\s*</record>" +
				"  </matrix>" +
				"</template>";
			Template template = new Template(CMLUtil.parseXML(templateS));
			String toBeParsed = "" +
				" 1.1 1.2\n" +
				" 2.1 2.1\n" +
				" 3.1 3.2\n";
			template.applyMarkup(toBeParsed);
			LineContainer lineContainer = template.getLineContainer();
			Assert.assertNotNull(lineContainer);
			String refS = "" +
				"<module cmlx:templateRef='m1' xmlns='http://www.xml-cml.org/schema' xmlns:cmlx='http://www.xml-cml.org/schema/cmlx'>" +
				"  <list cmlx:role='matrixContainer'>" +
				"    <list cmlx:role='body'>" +
				"      <matrix rows='3' columns='2' dataType='xsd:double' dictRef='x:x'>1.1 1.2 2.1 2.1 3.1 3.2</matrix>" +
				"    </list>" +
				"  </list>" +
				"</module>";
			JumboTestUtils.assertEqualsCanonically("Row", refS, lineContainer.getLinesElement(), true);
	}
	
	@Test
	public void testHeader() {
		String templateS = 
				"<template>" +
				"  <matrix id='m1'>" +
				"    <record id='r1' repeat='1' matrixFields='header'>\\s*{2I,x:col}\\s*</record>" +
				"  </matrix>" +
				"</template>";
			Template template = new Template(CMLUtil.parseXML(templateS));
			String toBeParsed = "" +
				"  1   2 \n" +
				"";
			template.applyMarkup(toBeParsed);
			LineContainer lineContainer = template.getLineContainer();
			Assert.assertNotNull(lineContainer);
			String refS = "" +
				"<module cmlx:templateRef='m1' xmlns='http://www.xml-cml.org/schema' xmlns:cmlx='http://www.xml-cml.org/schema/cmlx'>" +
				"  <list cmlx:role='matrixContainer'>" +
				"	 <list cmlx:role='header'>" +
				"	   <array dataType='xsd:integer' size='2' dictRef='x:col'>1 2</array>" +
				"	 </list>" +
				"  </list>" +
				"</module>";
			JumboTestUtils.assertEqualsCanonically("Row", refS, lineContainer.getLinesElement(), true);
	}
	
	
	@Test
	public void testDoubleHeaderWithoutMatrix() {
		String templateS = 
				"<template>" +
				"    <record id='r1' repeat='1' matrixFields='header'>\\s*{2I,x:col}\\s*</record>" +
				"    <record id='r2' repeat='1' matrixFields='header'>\\s*{2I,x:zzz}\\s*</record>" +
				"</template>";
			Template template = new Template(CMLUtil.parseXML(templateS));
			String toBeParsed = "" +
					"  1   2 \n" +
					"  99 100 \n" +
				"";
			template.applyMarkup(toBeParsed);
			LineContainer lineContainer = template.getLineContainer();
			Assert.assertNotNull(lineContainer);
			String refS = "" +
				"<module cmlx:templateRef='NULL_ID' xmlns='http://www.xml-cml.org/schema' xmlns:cmlx='http://www.xml-cml.org/schema/cmlx'>" +
				"  <list cmlx:templateRef='r1'>" +
				"    <array dataType='xsd:integer' size='2' dictRef='x:col'>1 2</array>" +
				"  </list>" +
				"  <list cmlx:templateRef='r2'>" +
				"    <array dataType='xsd:integer' size='2' dictRef='x:zzz'>99 100</array>" +
				"  </list>" +
				"</module>" +
				"";
			JumboTestUtils.assertEqualsCanonically("Row", refS, lineContainer.getLinesElement(), true);
	}
	
	@Test
	public void testDoubleHeader() {
		String templateS = 
				"<template>" +
				"  <matrix id='m1'>" +
				"    <record id='r1' repeat='1' matrixFields='header'>\\s*{2I,x:head1}\\s*</record>" +
				"    <record id='r2' repeat='1' matrixFields='header'>\\s*{2I,x:head2}\\s*</record>" +
				"  </matrix>" +
				"</template>";
			Template template = new Template(CMLUtil.parseXML(templateS));
			String toBeParsed = "" +
					"  1   2 \n" +
					"  99 100 \n" +
				"";
			template.applyMarkup(toBeParsed);
			LineContainer lineContainer = template.getLineContainer();
			Assert.assertNotNull(lineContainer);
			String refS = "" +
				"<module cmlx:templateRef='m1' xmlns='http://www.xml-cml.org/schema' xmlns:cmlx='http://www.xml-cml.org/schema/cmlx'>" +
				"  <list cmlx:role='matrixContainer'>" +
				"    <list cmlx:role='header'>" +
				"      <array dataType='xsd:integer' size='2' dictRef='x:head1'>1 2</array>" +
				"      <array dataType='xsd:integer' size='2' dictRef='x:head2'>99 100</array>" +
				"    </list>" +
				"  </list>" +
				"</module>";
			JumboTestUtils.assertEqualsCanonically("Row", refS, lineContainer.getLinesElement(), true);
	}
	
	public void testHeaderBody() {
		String templateS = 
				"<template>" +
				"  <matrix id='m1'>" +
				"    <record id='r1' repeat='1' matrixFields='header'>\\s*{2I,x:col}\\s*</record>" +
				"    <record id='r2' repeat='*' matrixFields='body'>\\s*{2F,x:x}\\s*</record>" +
				"  </matrix>" +
				"</template>";
			Template template = new Template(CMLUtil.parseXML(templateS));
			String toBeParsed = "" +
				"  1   2 \n" +
				" 1.1 1.2\n" +
				" 2.1 2.1\n" +
				" 3.1 3.2\n" +
				"";
			template.applyMarkup(toBeParsed);
			LineContainer lineContainer = template.getLineContainer();
			Assert.assertNotNull(lineContainer);
			String refS = "" +
				"<module cmlx:templateRef='m1' xmlns='http://www.xml-cml.org/schema' xmlns:cmlx='http://www.xml-cml.org/schema/cmlx'>" +
				"  <matrix rows='3' columns='2' dataType='xsd:double'>1.1 1.2 2.1 2.1 3.1 3.2</matrix>" +
				"</module>";
			JumboTestUtils.assertEqualsCanonically("Row", refS, lineContainer.getLinesElement(), true);
	}
	
	
	@Test
	public void testFooter0() {
		String templateS = 
				"<template>" +
				"  <matrix id='m1'>" +
				"    <record id='r4' repeat='1' matrixFields='footer'>\\s*{X,x:footer}\\s*</record>" +
				"  </matrix>" +
				"</template>";
			Template template = new Template(CMLUtil.parseXML(templateS));
			String toBeParsed = "" +
				" A footer\n";
			template.applyMarkup(toBeParsed);
			LineContainer lineContainer = template.getLineContainer();
			Assert.assertNotNull(lineContainer);
			String refS = "" +
				"<module cmlx:templateRef='m1' xmlns='http://www.xml-cml.org/schema' xmlns:cmlx='http://www.xml-cml.org/schema/cmlx'>" +
				"  <list cmlx:role='matrixContainer'>" +
				"    <list cmlx:role='footer'>" +
				"      <scalar dataType='xsd:string' dictRef='x:footer'>A footer</scalar>" +
				"    </list>" +
				"  </list>" +
				"</module>";
			JumboTestUtils.assertEqualsCanonically("Row", refS, lineContainer.getLinesElement(), true);
			
	}
	
	@Test
	public void testFooter() {
		String templateS = 
				"<template>" +
				"  <matrix id='m1'>" +
				"    <record id='r3' repeat='*' matrixFields='body'>\\s*{2F,x:x}\\s*</record>" +
				"    <record id='r4' repeat='1' matrixFields='footer'>\\s*{X,x:footer}\\s*</record>" +
				"  </matrix>" +
				"</template>";
			Template template = new Template(CMLUtil.parseXML(templateS));
			String toBeParsed = "" +
				" 1.1 1.2\n" +
				" 2.1 2.1\n" +
				" 3.1 3.2\n" +
				" A footer\n";
			template.applyMarkup(toBeParsed);
			LineContainer lineContainer = template.getLineContainer();
			Assert.assertNotNull(lineContainer);
			String refS = "" +
				"<module cmlx:templateRef='m1' xmlns='http://www.xml-cml.org/schema' xmlns:cmlx='http://www.xml-cml.org/schema/cmlx'>" +
				"  <list cmlx:role='matrixContainer'>" +
				"    <matrix rows='3' columns='2' dataType='xsd:double'>1.1 1.2 2.1 2.1 3.1 3.2</matrix>" +
				"    <list cmlx:role='footer'>" +
				"      <scalar dataType='xsd:string' dictRef='x:footer'>A footer</scalar>" +
				"    </list>" +
				"  </list>" +
				"</module>";
			JumboTestUtils.assertEqualsCanonically("Row", refS, lineContainer.getLinesElement(), true);
			
	}
	
	
	@Test
	public void testComplete() {
		String templateS = 
				"<template>" +
				"  <matrix id='m1'>" +
				"    <record id='r1' repeat='1' matrixFields='header'>\\s*{2I,x:head1}\\s*</record>" +
				"    <record id='r2' repeat='1' matrixFields='header'>\\s*{2A,x:head2}\\s*</record>" +
				"    <record id='r3' repeat='*' matrixFields='body'>\\s*{2F,x:x}\\s*</record>" +
				"    <record id='r4' repeat='1' matrixFields='footer'>\\s*{X,x:footer}\\s*</record>" +
				"  </matrix>" +
				"</template>";
			Template template = new Template(CMLUtil.parseXML(templateS));
			String toBeParsed = "" +
				"  1   2 \n" +
				"  A   B \n" +
				" 1.1 1.2\n" +
				" 2.1 2.1\n" +
				" 3.1 3.2\n" +
				" A footer\n";
			template.applyMarkup(toBeParsed);
			LineContainer lineContainer = template.getLineContainer();
			Assert.assertNotNull(lineContainer);
			String refS = "" +
				"<module cmlx:templateRef='m1' xmlns='http://www.xml-cml.org/schema' xmlns:cmlx='http://www.xml-cml.org/schema/cmlx'>" +
				"  <list cmlx:role='matrixContainer'>" +
				"    <list cmlx:role='header'>" +
				"      <array dataType='xsd:integer' dictRef='x:head1'>1 2</array>" +
				"      <array dataType='xsd:string' dictRef='x:head2'>A B</array>" +
				"    </list>" +
				"    <matrix rows='3' columns='2' dataType='xsd:double'>1.1 1.2 2.1 2.1 3.1 3.2</matrix>" +
				"    <list cmlx:role='footer'>" +
				"      <scalar dataType='xsd:string' dictRef='x:footer'>A footer</scalar>" +
				"    </list>" +
				"  </list>" +
				"</module>";
			JumboTestUtils.assertEqualsCanonically("Row", refS, lineContainer.getLinesElement(), true);
			
	}
}

