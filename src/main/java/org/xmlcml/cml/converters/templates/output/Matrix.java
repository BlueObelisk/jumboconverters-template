package org.xmlcml.cml.converters.templates.output;

import java.util.ArrayList;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.base.CMLUtil;
import org.xmlcml.cml.converters.Outputter;
import org.xmlcml.cml.converters.Outputter.OutputLevel;
import org.xmlcml.cml.converters.templates.output.LineReader.LineType;
import org.xmlcml.cml.element.CMLArray;
import org.xmlcml.cml.element.CMLList;
import org.xmlcml.cml.element.CMLMatrix;
import org.xmlcml.cml.element.CMLScalar;
import org.xmlcml.euclid.Util;

public class Matrix extends MarkupContainer {
	private final static Logger LOG = Logger.getLogger(Matrix.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	private static final String ROLE = "role";
	private static final String MATRIX = "__matrix";
	public static final String MATRIX_READER = "matrixReader";

	private static final String MATRIX_CONTAINER = "matrixContainer";
	private static final String BODY = "body";
	private static final String FOOTER = "footer";
	private static final String HEADER = "header";
	private static final String LEFT = "left";
	private static final String RIGHT = "right";
	static final String[] MATRIXROLES = {BODY, FOOTER, HEADER, LEFT, RIGHT};
	
	private static String prefix = "ChangeMe";

	private Integer rows = null;
	private Integer cols = null;

	private CMLArray matrixArray0;
	private String dataType;
	private CMLMatrix matrix;
	private double[] dvals;
	private int[] ivals;

	private String[] matrixFields;

	private Element linesElements;
	private CMLList headerList;
	private CMLList bodyList;
	private CMLList footerList;

	private CMLList matrixContainer;

	private String expected;
	private Element linesElement;
	private Nodes matrixArrayNodes;
	
	public Matrix(Element element) {
		this.templateElement1 = element;
		processChildElementsAndAttributes();
	}
	
	protected void processAttributes() {
		checkIfAttributeNamesAreAllowed(templateElement1, new String[]{
			DEBUG,
			DICT_REF,
			ID,
		});
				
		id = templateElement1.getAttributeValue(ID);
		if (id == null) {
			id = NULL_ID;
		}
		this.dictRef = templateElement1.getAttributeValue(DICT_REF);
		
		processNewline();
		
		outputLevel = Outputter.extractOutputLevel(this.templateElement1);
		LOG.trace(outputLevel+"/"+this.templateElement1.getAttributeValue(Outputter.OUTPUT));
		if (!OutputLevel.NONE.equals(outputLevel)) {
//			System.out.println("OUTPUT "+outputLevel);
		}
		debug = (templateElement1.getAttributeValue(DEBUG) != null);
	}


	protected void createSubclassedElementsFromChildElements() {
		Elements childElements = templateElement1.getChildElements();
		markerList = new ArrayList<MarkupApplier>();
		for (int i = 0; i < childElements.size(); i++) {
			LineReader lineReader = null;
			Element childElement = (Element) childElements.get(i);
			String name = childElement.getLocalName();
			if (name == null) {
				ignore();
			} else if (LineType.COMMENT.getTag().equals(name)) {
				ignore();
			} else if (LineType.RECORD.getTag().equals(name)) {
				lineReader = new RecordReader(childElement, this);
				markerList.add(lineReader);
			} else if (MarkupContainer.DEBUG.equals(name)) {
				markerList.add(new Debug(this));
			} else {
				MarkupApplier transformer = TransformElement.createTransformer(childElement);
				if (transformer != null) {
					markerList.add(transformer);
				} else {
					CMLUtil.debug(templateElement1, "UNKNOWN CHILD");
					throw new RuntimeException("unknown child: "+name);
				}
			}
		}
	}


	@Override
	public void applyMarkup(LineContainer lineContainer) {
		this.lineContainer = lineContainer;
		CMLElement element = null;
		int originalNodeIndex = lineContainer.getCurrentNodeIndex();
		linesElement = lineContainer.getLinesElement();
		copyNamespaces(linesElement);
		CMLElement.addCMLXAttribute(linesElement, Template.TEMPLATE_REF, this.getId());
		matrixContainer = new CMLList();
		headerList = createListAndAddToMatrixContainer(HEADER);
		bodyList = createListAndAddToMatrixContainer(BODY);
		footerList = createListAndAddToMatrixContainer(FOOTER);
		CMLElement.addCMLXAttribute(matrixContainer, ROLE, MATRIX_CONTAINER);
		expected = HEADER;
		for (MarkupApplier marker : markerList) {
			LOG.info("Applying: "+marker.getClass().getSimpleName()+" "+marker.getId());
			try {
				if (marker instanceof RecordReader) {
					processRecord(lineContainer, (RecordReader) marker);
				} else {
					processNonRecord(lineContainer, marker);
				}
			} catch (Exception e) {
				processException(lineContainer, marker, e);
			}
		}
		tidyMatrix();
	}

	private CMLList createListAndAddToMatrixContainer(String role) {
		CMLList list = new CMLList();
		CMLElement.addCMLXAttribute(list, ROLE, role);
		matrixContainer.appendChild(list);
		return list;
	}

	private void processRecord(LineContainer lineContainer, RecordReader recordReader) {
		// standard processing at present but might be optimized later
		recordReader.applyMarkup(lineContainer);
		processFields(recordReader, lineContainer);
	}

	private void processFields(RecordReader recordReader, LineContainer lineContainer) {
			matrixFields = recordReader.getMatrixFields();
			linesElements = lineContainer.getLinesElement();
			if (HEADER.equals(expected)) {
				if (Util.containsString(matrixFields, HEADER)) {
					processHeaderRecord();
				} else if (Util.containsString(matrixFields, BODY)) {
					expected = BODY;
					processBodyRecord();
				} else if (Util.containsString(matrixFields, FOOTER)) {
					throw new RuntimeException("Missing body");
				}
			} else if (BODY.equals(expected)) {
				if (Util.containsString(matrixFields, BODY)) {
					throw new RuntimeException("Can only have one body");
				} else if (Util.containsString(matrixFields, FOOTER)) {
					expected = FOOTER;
					processFooterRecord();
				} else {
					throw new RuntimeException("Bad processing order:"+expected);
				}
			} else if (FOOTER.equals(expected)) {
				if (Util.containsString(matrixFields, FOOTER)) {
					processFooterRecord();
				} else {
					throw new RuntimeException("Bad processing order:"+expected);
				}
			} else {
				throw new RuntimeException("all <record> children of <matrix> must have known fields");
			}
	}

	private void processHeaderRecord() {
		ensureHeaderList();
		addArrayOrScalar(headerList, HEADER);
	}

	private void addArrayOrScalar(CMLList hfList, String role) {
		Nodes listNodes = linesElements.query("./cml:list[cml:array | cml:scalar]", CMLUtil.CML_XPATH);
		if (listNodes.size() == 0) {
			throw new RuntimeException("empty list");
		}
		CMLElement list = (CMLElement) listNodes.get(0);
		Nodes childNodes = list.query("./cml:array | ./cml:scalar", CMLUtil.CML_XPATH);
		if (childNodes.size() != 1) {
			throw new RuntimeException("Header/footer record must have exactly one <scalar> or one <array>");
		}
		CMLElement childNode = (CMLElement) childNodes.get(0);
		childNode.detach();
		hfList.appendChild(childNode);
		hfList.detach();
		matrixContainer.appendChild((CMLElement)hfList);
	}

	private void ensureHeaderList() {
		if (headerList == null) {
			headerList = new CMLList();
			CMLElement.addCMLXAttribute(headerList, ROLE, HEADER);
		}
	}

	private void processBodyRecord() {
		Nodes listNodes = linesElements.query(".//cml:list[cml:array]", CMLUtil.CML_XPATH);
		Element list = (Element) listNodes.get(0);
		if (list == null) {
			throw new RuntimeException("body requires arrays with list parent");
		}
		if (matrix == null) {
			createMatrix(list, listNodes);
		}
	}

	private void processFooterRecord() {
		ensureFooterList();
		addArrayOrScalar(footerList, FOOTER);
	}

	private void ensureFooterList() {
		if (footerList == null) {
			footerList = new CMLList();
			CMLElement.addCMLXAttribute(footerList, ROLE, FOOTER);
		}
	}

	private void processNonRecord(LineContainer lineContainer, MarkupApplier marker) {
		marker.applyMarkup(lineContainer);
		Element linesContainer = lineContainer.getLinesElement();
		linesContainer.detach();
		matrixContainer.appendChild(linesContainer);
	}

	private void createMatrix(Element list, Nodes listNodes) {
		matrixArrayNodes = listNodes.get(0).query("./cml:array", CMLUtil.CML_XPATH);
		matrixArray0 = (CMLArray) matrixArrayNodes.get(0);
		cols = matrixArray0.getSize();
		rows  = matrixArrayNodes.size();
		dataType = matrixArray0.getDataType();
		prepareForNumericValues();
		extractNumericValues(matrixArrayNodes);
		createAndPopulateMatrix();
		matrix.setDictRef(matrixArray0.getDictRef());
//		list.getParent().replaceChild(list, matrix);
		list.appendChild(matrix);
		listNodes.get(0).detach();
	}

	private void createAndPopulateMatrix() {
		if (CMLUtil.XSD_DOUBLE.equals(dataType)) {
			matrix = new CMLMatrix(rows, cols, dvals);
		} else if (CMLUtil.XSD_INTEGER.equals(dataType)) {
			matrix = new CMLMatrix(rows, cols, ivals);
		} else {
			throw new RuntimeException("cannot create non-numeric matrix");
		}
	}

	private void extractNumericValues(Nodes arrayNodes) {
		for (int irow = 0; irow <  arrayNodes.size(); irow++) {
			CMLArray array = (CMLArray) arrayNodes.get(irow);
			if (CMLUtil.XSD_DOUBLE.equals(dataType)) {
				System.arraycopy(array.getDoubles(), 0, dvals, irow * cols, cols);
			} else if (CMLUtil.XSD_INTEGER.equals(dataType)) {
				System.arraycopy(array.getInts(), 0, ivals, irow * cols, cols);
			}
		}
	}

	private void prepareForNumericValues() {
		if (CMLUtil.XSD_DOUBLE.equals(dataType)) {
			dvals = new double[rows * cols];
		} else if (CMLUtil.XSD_INTEGER.equals(dataType)) {
			ivals = new int[rows * cols];
		} else {
			throw new RuntimeException("cannot create non-numeric matrix");
		}
	}

	private void tidyMatrix() {
		linesElement.appendChild(matrixContainer);
		matrix.detach();
		// insert after header
		int headerRole = matrixContainer.query("./cml:list[@*[local-name()='role' and .='header']]", CMLUtil.CML_XPATH).size();
		matrixContainer.insertChild(matrix, headerRole);
	}


	@Override
	public void applyMarkup(Element element) {
		// TODO Auto-generated method stub
		
	}





}
