package org.xmlcml.cml.converters.templates.output;

import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.cml.base.CMLConstants;
import org.xmlcml.cml.base.CMLUtil;
import org.xmlcml.cml.converters.Outputter.OutputLevel;

import nu.xom.Element;

public abstract class MarkupContainer implements MarkupApplier {
	private final static Logger LOG = Logger.getLogger(MarkupContainer.class);
	static{
	    LOG.setLevel(Level.ERROR);
	}

	private static final String HTTP_WWW_W3_ORG_2001_X_INCLUDE = "http://www.w3.org/2001/XInclude";
	protected static final String DEBUG = "debug";
	protected static final String DICT_REF = "dictRef";
	protected static final String ID = "id";
	protected static final String NULL_ID = "NULL_ID";
	protected static final String NEWLINE = "newline";
	private static final String DEFAULT_NEWLINE = CMLConstants.S_DOLLAR;
	protected static final String MULTIPLE = "multiple"; 
	
	protected String id;
	protected String name;
	protected List<MarkupApplier> markerList;
	protected LineContainer lineContainer;
	protected Element templateElement1;
	protected OutputLevel outputLevel;
	protected String dictRef;
	protected boolean debug = false;
	protected String newlineS;

	public MarkupContainer() {
		
	}
	
	public MarkupContainer(Element element) {
		this.templateElement1 = element;
	}

	protected void processChildElementsAndAttributes() {
		processAttributes();
		createSubclassedElementsFromChildElements();
		if (!OutputLevel.NONE.equals(outputLevel)) {
//			this.debug();
		}
	}

	protected abstract void processAttributes();
	protected abstract void createSubclassedElementsFromChildElements();
	
	public String getId() {
		return id;
	}

	protected void ignore() {
	}

	public void debug() {
		// TODO Auto-generated method stub
		
	}

	protected void copyNamespaces(Element targetElement) {
		copyNamespaces(this.templateElement1, targetElement);
	}

	protected void processException(LineContainer lineContainer, MarkupApplier marker, Exception e) {
	//		lineContainer.debug("LINE CONTAINER");
			String line = lineContainer.peekLine();
			int nline = lineContainer.getCurrentNodeIndex();
			System.err.println("PREVIOUS..."+nline);
			for (int i = (Math.max(0, nline-6)); i < nline; i++) {
				System.err.println(lineContainer.getLinesElement().getChild(i));
			}
			System.out.println("========DEBUG======="+marker.getId()+">>>>>");
	//		CMLUtil.debug(templateElement1, "TEMPLATE");
			System.out.println("<<<<<"+marker.getId()+"========DEBUG=======");
			if (line == null) {
				LOG.error("Null line ("+nline+")", e);
			} else {
				throw new RuntimeException("Failed; current line ("+nline+")"+line, e);
			}
		}

	public static void copyNamespaces(Element fromElement, Element targetElement) {
		int count = fromElement.getNamespaceDeclarationCount();
		for (int i = 0; i < count; i++) {
			String prefix = fromElement.getNamespacePrefix(i);
			String namespaceURI = fromElement.getNamespaceURI(prefix);
			if (!(HTTP_WWW_W3_ORG_2001_X_INCLUDE.equals(namespaceURI))) {
				if (!hasNamespacePrefix(targetElement, prefix)) {
					targetElement.addNamespaceDeclaration(prefix, namespaceURI);
				}
			}
		}
	}

	private static boolean hasNamespacePrefix(Element targetElement, String prefix) {
		for (int i = 0; i < targetElement.getNamespaceDeclarationCount(); i++) {
			if (prefix.equals(targetElement.getNamespacePrefix(i))) {
				return true;
			}
		}
		return false;
	}

	public static void checkIfAttributeNamesAreAllowed(Element theElement,
		String[] allowedNames) {
			for (int i = 0; i < theElement.getAttributeCount(); i++) {
				String attName = theElement.getAttribute(i).getLocalName();
				boolean allowed = false;
				Exception exception = null;
				for (String allowedName : allowedNames) {
					if (attName.equals(allowedName)) {
						allowed = true;
						break;
					}
				}
				if (!allowed) {
					System.out.println("Allowed options:");
					for (String name : allowedNames) {
						System.out.println(">     "+name);
					}
					CMLUtil.debug(theElement, "FORBIDDEN ATT "+attName);
					throw new RuntimeException("Forbidden attribute name: "+attName);
				}
			}
		}

	public static String escape(String s) {
		if ("\"$%^*-+.".contains(s)) {
			s =  CMLConstants.S_BACKSLASH+s;
		}
		return s;
	}

	protected void processNewline() {
		newlineS = templateElement1.getAttributeValue(NEWLINE);
		if (newlineS == null) {
			newlineS = templateElement1.getAttributeValue(MULTIPLE);
			if (newlineS != null) {
				LOG.warn("multiple is deprecated, use newline");
			}
		}
		if (newlineS == null) {
			newlineS = DEFAULT_NEWLINE;
		}
		if (newlineS != null) {
			newlineS = escape(newlineS);
		}
	}


}
