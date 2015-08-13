package com.anli.generalization.data;

import com.anli.generalization.data.access.DataObjectProvider;
import com.anli.generalization.data.access.metadata.AttributeProvider;
import com.anli.generalization.data.access.metadata.ListEntryProvider;
import com.anli.generalization.data.access.metadata.ObjectTypeProvider;
import com.anli.generalization.data.entities.DataObject;
import com.anli.generalization.data.entities.metadata.Attribute;
import com.anli.generalization.data.entities.metadata.ListEntry;
import com.anli.generalization.data.entities.metadata.ObjectType;
import com.anli.generalization.data.factory.JpaProviderFactory;
import com.anli.generalization.data.utils.AttributeHelper;
import com.anli.generalization.data.utils.DataObjectHelper;
import com.anli.generalization.data.utils.ListEntryHelper;
import com.anli.generalization.data.utils.ObjectTypeHelper;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.anli.generalization.data.entities.metadata.AttributeType.DATE;
import static com.anli.generalization.data.entities.metadata.AttributeType.LIST;
import static com.anli.generalization.data.entities.metadata.AttributeType.REFERENCE;
import static com.anli.generalization.data.entities.metadata.AttributeType.TEXT;
import static com.anli.generalization.data.utils.CommonDeployment.getDeployment;
import static com.anli.generalization.data.utils.ValueFactory.bi;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class DataObjectParametersTest {

    @Deployment
    public static Archive createDeployment() {
        return getDeployment();
    }

    private static final long TYPE_A = 1501;
    private static final long TYPE_B = 1502;
    private static final long TYPE_C = 1503;

    private static final long OBJECT_A = 1504;
    private static final long OBJECT_B = 1505;
    private static final long OBJECT_C = 1506;

    private static final long S_TEXT_ATTR = 1507;
    private static final long S_DATE_ATTR = 1508;
    private static final long S_REF_ATTR = 1509;
    private static final long S_LIST_ATTR = 1510;
    private static final long M_TEXT_ATTR = 1511;
    private static final long M_DATE_ATTR = 1512;
    private static final long M_REF_ATTR = 1513;
    private static final long M_LIST_ATTR = 1514;
    private static final long S_R_REF_ATTR = 1515;
    private static final long M_R_REF_ATTR = 1516;

    private static final long S_LIST_A = 1517;
    private static final long S_LIST_B = 1518;
    private static final long S_LIST_C = 1519;
    private static final long M_LIST_A = 1520;
    private static final long M_LIST_B = 1521;
    private static final long M_LIST_C = 1522;
    @Resource(lookup = "java:/jdbc/integration_testing")
    private DataSource dataSource;

    @Resource
    private UserTransaction transaction;

    private ObjectTypeHelper typeHelper;
    private AttributeHelper attrHelper;
    private ListEntryHelper listHelper;
    private DataObjectHelper objectHelper;

    private ObjectTypeProvider typeProvider;
    private AttributeProvider attrProvider;
    private ListEntryProvider listProvider;
    private DataObjectProvider objectProvider;

    @Before
    public void setUp() {
        typeHelper = new ObjectTypeHelper(dataSource);
        attrHelper = new AttributeHelper(dataSource);
        listHelper = new ListEntryHelper(dataSource);
        objectHelper = new DataObjectHelper(dataSource);
        typeProvider = JpaProviderFactory.getInstance().getObjectTypeProvider();
        attrProvider = JpaProviderFactory.getInstance().getAttributeProvider();
        listProvider = JpaProviderFactory.getInstance().getListEntryProvider();
        objectProvider = JpaProviderFactory.getInstance().getDataObjectProvider();

        prepareTypes();
        prepareReferences();
        prepareAttributes();
        prepareListEntries();
    }

    protected void prepareTypes() {
        if (typeHelper.readObjectType(bi(TYPE_A)) == null) {
            typeHelper.createObjectType(TYPE_A, "Type A");
        }
        if (typeHelper.readObjectType(bi(TYPE_B)) == null) {
            typeHelper.createObjectType(TYPE_B, "Type B");
        }
        if (typeHelper.readObjectType(bi(TYPE_C)) == null) {
            typeHelper.createObjectType(TYPE_C, "Type C");
        }
        typeHelper.linkObjectTypesToParent(TYPE_B, TYPE_C);
    }

    protected void prepareReferences() {
        if (objectHelper.readObject(bi(OBJECT_A)) == null) {
            objectHelper.createObject(OBJECT_A, "Object A", null, bi(TYPE_A), null);
        }
        if (objectHelper.readObject(bi(OBJECT_B)) == null) {
            objectHelper.createObject(OBJECT_B, "Object B", null, bi(TYPE_B), null);
        }
        if (objectHelper.readObject(bi(OBJECT_C)) == null) {
            objectHelper.createObject(OBJECT_C, "Object C", null, bi(TYPE_C), null);
        }
    }

    protected void prepareAttributes() {
        if (attrHelper.readAttribute(bi(S_TEXT_ATTR)) == null) {
            attrHelper.createAttribute(S_TEXT_ATTR, TEXT, "Single Text", false, null);
        }
        if (attrHelper.readAttribute(bi(S_DATE_ATTR)) == null) {
            attrHelper.createAttribute(S_DATE_ATTR, DATE, "Single Date", false, null);
        }
        if (attrHelper.readAttribute(bi(S_REF_ATTR)) == null) {
            attrHelper.createAttribute(S_REF_ATTR, REFERENCE, "Single Reference", false, null);
        }
        if (attrHelper.readAttribute(bi(S_LIST_ATTR)) == null) {
            attrHelper.createAttribute(S_LIST_ATTR, LIST, "Single List", false, null);
        }
        if (attrHelper.readAttribute(bi(M_TEXT_ATTR)) == null) {
            attrHelper.createAttribute(M_TEXT_ATTR, TEXT, "Multiple Text", true, null);
        }
        if (attrHelper.readAttribute(bi(M_DATE_ATTR)) == null) {
            attrHelper.createAttribute(M_DATE_ATTR, DATE, "Multiple Date", true, null);
        }
        if (attrHelper.readAttribute(bi(M_REF_ATTR)) == null) {
            attrHelper.createAttribute(M_REF_ATTR, REFERENCE, "Multiple Reference", true, null);
        }
        if (attrHelper.readAttribute(bi(M_LIST_ATTR)) == null) {
            attrHelper.createAttribute(M_LIST_ATTR, LIST, "Multiple List", true, null);
        }
        if (attrHelper.readAttribute(bi(S_R_REF_ATTR)) == null) {
            attrHelper.createAttribute(S_R_REF_ATTR, REFERENCE, "Single Restricted Reference",
                    false, bi(TYPE_B));
        }
        if (attrHelper.readAttribute(bi(M_R_REF_ATTR)) == null) {
            attrHelper.createAttribute(M_R_REF_ATTR, REFERENCE, "Multiple Restricted Reference",
                    true, bi(TYPE_B));
        }
        attrHelper.linkAttributesToObjectType(TYPE_A, S_TEXT_ATTR, S_DATE_ATTR, S_REF_ATTR, S_LIST_ATTR,
                M_TEXT_ATTR, M_DATE_ATTR, M_REF_ATTR, M_LIST_ATTR, S_R_REF_ATTR, M_R_REF_ATTR);
    }

    protected void prepareListEntries() {
        if (listHelper.readListEntry(bi(S_LIST_A)) == null) {
            listHelper.createListEntry(S_LIST_A, "SList A");
        }
        if (listHelper.readListEntry(bi(S_LIST_B)) == null) {
            listHelper.createListEntry(S_LIST_B, "SList B");
        }
        if (listHelper.readListEntry(bi(S_LIST_C)) == null) {
            listHelper.createListEntry(S_LIST_C, "SList C");
        }
        listHelper.linkListEntriesToAttribute(S_LIST_ATTR, S_LIST_A, S_LIST_B, S_LIST_C);
        if (listHelper.readListEntry(bi(M_LIST_A)) == null) {
            listHelper.createListEntry(M_LIST_A, "MList A");
        }
        if (listHelper.readListEntry(bi(M_LIST_B)) == null) {
            listHelper.createListEntry(M_LIST_B, "MList B");
        }
        if (listHelper.readListEntry(bi(M_LIST_C)) == null) {
            listHelper.createListEntry(M_LIST_C, "MList C");
        }
        listHelper.linkListEntriesToAttribute(M_LIST_ATTR, M_LIST_A, M_LIST_B, M_LIST_C);
    }

    @Test
    @InSequence(0)
    public void testCreation_shouldNotCreateParameters() throws Exception {
        transaction.begin();

        ObjectType type = typeProvider.getById(bi(TYPE_A));
        DataObject dataObject = objectProvider.create(type);
        BigInteger id = dataObject.getId();

        transaction.commit();

        Collection<BigInteger> parameters = objectHelper.readParametersByObject(id);

        assertTrue(parameters.isEmpty());
    }

    @Test
    @InSequence(1)
    public void testReading_shouldReadSingleParameters() throws Exception {
        objectHelper.createObject(1530, "Single Param Object", null, bi(TYPE_A), null);
        objectHelper.createParameter(1531, bi(S_TEXT_ATTR), bi(1530));
        objectHelper.createTextValue(1532, "Single Text Value");
        objectHelper.linkValuesToParameter(1531, 1532);
        objectHelper.createParameter(1533, bi(S_DATE_ATTR), bi(1530));
        objectHelper.createDateValue(1534, 1439405805L);
        objectHelper.linkValuesToParameter(1533, 1534);
        objectHelper.createParameter(1535, bi(S_REF_ATTR), bi(1530));
        objectHelper.createReferenceValue(1536, bi(OBJECT_A));
        objectHelper.linkValuesToParameter(1535, 1536);
        objectHelper.createParameter(1537, bi(S_LIST_ATTR), bi(1530));
        objectHelper.createListValue(1538, bi(S_LIST_A));
        objectHelper.linkValuesToParameter(1537, 1538);

        transaction.begin();

        Attribute text = attrProvider.getById(bi(S_TEXT_ATTR));
        Attribute date = attrProvider.getById(bi(S_DATE_ATTR));
        Attribute ref = attrProvider.getById(bi(S_REF_ATTR));
        Attribute list = attrProvider.getById(bi(S_LIST_ATTR));
        Attribute rRef = attrProvider.getById(bi(S_R_REF_ATTR));

        DataObject dataObject = objectProvider.getById(bi(1530));
        String textValue = dataObject.getValue(text);
        DateTime dateValue = dataObject.getValue(date);
        DataObject referenceValue = dataObject.getValue(ref);
        ListEntry listValue = dataObject.getValue(list);
        BigInteger referenceId = referenceValue.getId();
        String referenceName = referenceValue.getName();
        BigInteger listId = listValue.getId();
        String listEntryValue = listValue.getEntryValue();
        DataObject restrictedReference = dataObject.getValue(rRef);

        transaction.commit();

        assertEquals("Single Text Value", textValue);
        assertEquals(1439405805L, dateValue.getMillis());
        assertEquals(bi(OBJECT_A), referenceId);
        assertEquals("Object A", referenceName);
        assertEquals(bi(S_LIST_A), listId);
        assertEquals("SList A", listEntryValue);
        assertNull(restrictedReference);
    }

    @Test
    @InSequence(2)
    public void testReading_shouldReadMultipleParameters() throws Exception {
        objectHelper.createObject(1540, "Multiple Param Object", null, bi(TYPE_A), null);
        objectHelper.createParameter(1541, bi(M_TEXT_ATTR), bi(1540));
        objectHelper.createTextValue(1544, "Text Value 1");
        objectHelper.createTextValue(1543, "Text Value 2");
        objectHelper.createTextValue(1542, "Text Value 3");
        objectHelper.linkValuesToParameter(1541, 1544, 1543, 1542);
        objectHelper.createParameter(1545, bi(M_DATE_ATTR), bi(1540));
        objectHelper.createDateValue(1546, 1439405805L);
        objectHelper.createDateValue(1547, 1439407288L);
        objectHelper.linkValuesToParameter(1545, 1546, 1547);
        objectHelper.createParameter(1548, bi(M_REF_ATTR), bi(1540));
        objectHelper.createReferenceValue(1549, bi(OBJECT_A));
        objectHelper.createReferenceValue(1550, bi(OBJECT_B));
        objectHelper.linkValuesToParameter(1548, 1550, 1549);
        objectHelper.createParameter(1551, bi(M_LIST_ATTR), bi(1540));
        objectHelper.createListValue(1552, bi(M_LIST_B));
        objectHelper.linkValuesToParameter(1551, 1552);

        transaction.begin();

        Attribute text = attrProvider.getById(bi(M_TEXT_ATTR));
        Attribute date = attrProvider.getById(bi(M_DATE_ATTR));
        Attribute ref = attrProvider.getById(bi(M_REF_ATTR));
        Attribute list = attrProvider.getById(bi(M_LIST_ATTR));
        Attribute restRef = attrProvider.getById(bi(M_R_REF_ATTR));

        DataObject dataObject = objectProvider.getById(bi(1540));
        List<String> textValues = dataObject.getValue(text);
        List<DateTime> dateValues = dataObject.getValue(date);
        List<DataObject> referenceValues = dataObject.getValue(ref);
        Collection<ListEntry> listValues = dataObject.getValue(list);
        Collection<DataObject> restrictedRefValues = dataObject.getValue(restRef);

        assertEquals(3, textValues.size());
        assertEquals(2, dateValues.size());
        assertEquals(2, referenceValues.size());
        assertEquals(1, listValues.size());
        assertTrue(restrictedRefValues.isEmpty());

        String firstTextValue = textValues.get(0);
        String secondTextValue = textValues.get(1);
        String thirdTextValue = textValues.get(2);
        DateTime firstDateValue = dateValues.get(0);
        DateTime secondDateValue = dateValues.get(1);
        DataObject firstRef = referenceValues.get(0);
        BigInteger firstRefId = firstRef.getId();
        DataObject secondRef = referenceValues.get(1);
        BigInteger secondRefId = secondRef.getId();
        ListEntry listValue = listValues.iterator().next();
        BigInteger listId = listValue.getId();

        transaction.commit();

        assertEquals("Text Value 1", firstTextValue);
        assertEquals("Text Value 2", secondTextValue);
        assertEquals("Text Value 3", thirdTextValue);
        assertEquals(1439405805L, firstDateValue.getMillis());
        assertEquals(1439407288L, secondDateValue.getMillis());
        assertEquals(bi(OBJECT_B), firstRefId);
        assertEquals(bi(OBJECT_A), secondRefId);
        assertEquals(bi(M_LIST_B), listId);
    }

    @Test(expected = ClassCastException.class)
    @InSequence(3)
    public void testReading_shouldNotCastIncompatibleValueTypes() throws Exception {
        objectHelper.createObject(1553, "Single Param Object", null, bi(TYPE_A), null);
        objectHelper.createParameter(1554, bi(S_TEXT_ATTR), bi(1553));
        objectHelper.createTextValue(1555, "Single Text Value");
        objectHelper.linkValuesToParameter(1554, 1555);

        transaction.begin();

        Attribute textAttr = attrProvider.getById(bi(S_TEXT_ATTR));
        DataObject dataObject = objectProvider.getById(bi(1553));
        ListEntry entry = dataObject.getValue(textAttr);
        entry.getId();

        transaction.commit();
    }

    @Test(expected = IllegalArgumentException.class)
    @InSequence(4)
    public void testReading_shouldCheckNullAttribute() throws Exception {
        objectHelper.createObject(1556, "Param Object", null, bi(TYPE_A), null);

        transaction.begin();

        DataObject dataObject = objectProvider.getById(bi(1556));
        dataObject.getValue(null);

        transaction.commit();
    }

    @Test
    @InSequence(5)
    public void testSetSingleValue_shouldCreateSingleParameter() throws Exception {
        objectHelper.createObject(1557, "Empty Object", null, bi(TYPE_A), null);

        transaction.begin();

        Attribute text = attrProvider.getById(bi(S_TEXT_ATTR));
        Attribute date = attrProvider.getById(bi(M_DATE_ATTR));
        Attribute ref = attrProvider.getById(bi(S_REF_ATTR));
        Attribute list = attrProvider.getById(bi(M_LIST_ATTR));
        Attribute restRef = attrProvider.getById(bi(S_R_REF_ATTR));

        DataObject dataObject = objectProvider.getById(bi(1557));
        DataObject reference = objectProvider.getById(bi(OBJECT_C));
        ListEntry listEntry = listProvider.getById(bi(M_LIST_C));

        dataObject.setSingleValue(text, "Text Value");
        dataObject.setSingleValue(date, new DateTime(1439409164L));
        dataObject.setSingleValue(ref, reference);
        dataObject.setSingleValue(list, listEntry);
        dataObject.setSingleValue(restRef, null);

        transaction.commit();

        Collection<BigInteger> parameters = objectHelper.readParametersByObject(bi(1557));

        assertEquals(4, parameters.size());
        BigInteger textParameter = null;
        BigInteger dateParameter = null;
        BigInteger refParameter = null;
        BigInteger listParameter = null;

        for (BigInteger parameter : parameters) {
            Map<String, Object> parameterData = objectHelper.readParameter(parameter);
            assertEquals(bi(1557), parameterData.get("object"));
            BigInteger paramAttr = (BigInteger) parameterData.get("attribute");
            BigInteger paramId = (BigInteger) parameterData.get("id");
            if (bi(S_TEXT_ATTR).equals(paramAttr)) {
                textParameter = paramId;
            } else if (bi(M_DATE_ATTR).equals(paramAttr)) {
                dateParameter = paramId;
            } else if (bi(S_REF_ATTR).equals(paramAttr)) {
                refParameter = paramId;
            } else if (bi(M_LIST_ATTR).equals(paramAttr)) {
                listParameter = paramId;
            } else {
                fail(paramAttr.toString());
            }
        }
        if (textParameter == null) {
            fail();
            return;
        }
        if (dateParameter == null) {
            fail();
            return;
        }
        if (refParameter == null) {
            fail();
            return;
        }
        if (listParameter == null) {
            fail();
            return;
        }

        List<BigInteger> textValues = objectHelper.readValuesByParameter(textParameter);
        List<BigInteger> dateValues = objectHelper.readValuesByParameter(dateParameter);
        List<BigInteger> refValues = objectHelper.readValuesByParameter(refParameter);
        List<BigInteger> listValues = objectHelper.readValuesByParameter(listParameter);

        assertEquals(1, textValues.size());
        assertEquals(1, dateValues.size());
        assertEquals(1, refValues.size());
        assertEquals(1, listValues.size());

        Map<String, Object> textData = objectHelper.readValue(textValues.iterator().next());
        Map<String, Object> dateData = objectHelper.readValue(dateValues.iterator().next());
        Map<String, Object> refData = objectHelper.readValue(refValues.iterator().next());
        Map<String, Object> listData = objectHelper.readValue(listValues.iterator().next());

        assertEquals(0, textData.get("type"));
        assertEquals("Text Value", textData.get("text"));
        assertNull(textData.get("date"));
        assertNull(textData.get("reference"));
        assertNull(textData.get("listEntry"));
        assertEquals(0, textData.get("order"));

        assertEquals(1, dateData.get("type"));
        assertNull(dateData.get("text"));
        assertEquals(bi(1439409164), dateData.get("date"));
        assertNull(dateData.get("reference"));
        assertNull(dateData.get("listEntry"));
        assertEquals(0, textData.get("order"));

        assertEquals(2, refData.get("type"));
        assertNull(refData.get("text"));
        assertNull(refData.get("date"));
        assertEquals(bi(OBJECT_C), refData.get("reference"));
        assertNull(refData.get("listEntry"));
        assertEquals(0, refData.get("order"));

        assertEquals(3, listData.get("type"));
        assertNull(listData.get("text"));
        assertNull(listData.get("date"));
        assertNull(listData.get("reference"));
        assertEquals(bi(M_LIST_C), listData.get("listEntry"));
        assertEquals(0, listData.get("order"));
    }

    @Test
    @InSequence(6)
    public void testSetSingleValue_shouldSetNewSingleValues() throws Exception {
        objectHelper.createObject(1558, "Single Param Object", null, bi(TYPE_A), null);
        objectHelper.createParameter(1559, bi(M_TEXT_ATTR), bi(1558));
        objectHelper.createTextValue(1560, "Initial Text Value 1");
        objectHelper.createTextValue(1561, "Initial Text Value 2");
        objectHelper.linkValuesToParameter(1559, 1560, 1561);
        objectHelper.createParameter(1562, bi(S_DATE_ATTR), bi(1558));
        objectHelper.createDateValue(1563, 1439489096L);
        objectHelper.linkValuesToParameter(1562, 1563);
        objectHelper.createParameter(1564, bi(M_REF_ATTR), bi(1558));
        objectHelper.createReferenceValue(1565, bi(OBJECT_A));
        objectHelper.createReferenceValue(1566, bi(OBJECT_B));
        objectHelper.linkValuesToParameter(1564, 1565, 1566);
        objectHelper.createParameter(1567, bi(S_LIST_ATTR), bi(1558));
        objectHelper.createListValue(1568, bi(S_LIST_A));
        objectHelper.linkValuesToParameter(1567, 1568);

        transaction.begin();

        Attribute text = attrProvider.getById(bi(M_TEXT_ATTR));
        Attribute date = attrProvider.getById(bi(S_DATE_ATTR));
        Attribute ref = attrProvider.getById(bi(M_REF_ATTR));
        Attribute list = attrProvider.getById(bi(S_LIST_ATTR));

        DataObject referenceObject = objectProvider.getById(bi(OBJECT_C));
        ListEntry listEntry = listProvider.getById(bi(S_LIST_C));

        DataObject dataObject = objectProvider.getById(bi(1558));
        dataObject.setSingleValue(text, "Updated Text Value");
        dataObject.setSingleValue(date, new DateTime(1439489586));
        dataObject.setSingleValue(ref, referenceObject);
        dataObject.setSingleValue(list, listEntry);

        transaction.commit();

        Collection<BigInteger> parameters = objectHelper.readParametersByObject(bi(1558));
        Map<String, Object> textParameter = objectHelper.readParameter(bi(1559));
        Map<String, Object> dateParameter = objectHelper.readParameter(bi(1562));
        Map<String, Object> refParameter = objectHelper.readParameter(bi(1564));
        Map<String, Object> listParameter = objectHelper.readParameter(bi(1567));
        List<BigInteger> textValues = objectHelper.readValuesByParameter(bi(1559));
        List<BigInteger> dateValues = objectHelper.readValuesByParameter(bi(1562));
        List<BigInteger> refValues = objectHelper.readValuesByParameter(bi(1564));
        List<BigInteger> listValues = objectHelper.readValuesByParameter(bi(1567));
        Map<String, Object> firstTextValue = objectHelper.readValue(bi(1560));
        Map<String, Object> secondTextValue = objectHelper.readValue(bi(1561));
        Map<String, Object> dateValue = objectHelper.readValue(bi(1563));
        Map<String, Object> firstRefValue = objectHelper.readValue(bi(1565));
        Map<String, Object> secondRefValue = objectHelper.readValue(bi(1566));
        Map<String, Object> listValue = objectHelper.readValue(bi(1568));

        assertEquals(4, parameters.size());
        assertTrue(parameters.contains(bi(1559)));
        assertTrue(parameters.contains(bi(1562)));
        assertTrue(parameters.contains(bi(1564)));
        assertTrue(parameters.contains(bi(1567)));

        assertEquals(1, textValues.size());
        assertEquals(1, dateValues.size());
        assertEquals(1, refValues.size());
        assertEquals(1, listValues.size());
        assertEquals(bi(1560), textValues.iterator().next());
        assertEquals(bi(1563), dateValues.iterator().next());
        assertEquals(bi(1565), refValues.iterator().next());
        assertEquals(bi(1568), listValues.iterator().next());

        assertEquals(bi(M_TEXT_ATTR), textParameter.get("attribute"));
        assertEquals(bi(S_DATE_ATTR), dateParameter.get("attribute"));
        assertEquals(bi(M_REF_ATTR), refParameter.get("attribute"));
        assertEquals(bi(S_LIST_ATTR), listParameter.get("attribute"));

        assertEquals(0, firstTextValue.get("type"));
        assertEquals("Updated Text Value", firstTextValue.get("text"));
        assertNull(firstTextValue.get("date"));
        assertNull(firstTextValue.get("reference"));
        assertNull(firstTextValue.get("listEntry"));
        assertEquals(0, firstTextValue.get("order"));

        assertNull(secondTextValue);

        assertEquals(1, dateValue.get("type"));
        assertNull(dateValue.get("text"));
        assertEquals(bi(1439489586), dateValue.get("date"));
        assertNull(dateValue.get("reference"));
        assertNull(dateValue.get("listEntry"));
        assertEquals(0, dateValue.get("order"));

        assertEquals(2, firstRefValue.get("type"));
        assertNull(firstRefValue.get("text"));
        assertNull(firstRefValue.get("date"));
        assertEquals(bi(OBJECT_C), firstRefValue.get("reference"));
        assertNull(firstRefValue.get("listEntry"));
        assertEquals(0, firstRefValue.get("order"));

        assertNull(secondRefValue);

        assertEquals(3, listValue.get("type"));
        assertNull(listValue.get("text"));
        assertNull(listValue.get("date"));
        assertNull(listValue.get("reference"));
        assertEquals(bi(S_LIST_C), listValue.get("listEntry"));
        assertEquals(0, listValue.get("order"));
    }

    @Test
    @InSequence(7)
    public void testSetSingleValue_shouldRemoveSingleParameter() throws Exception {
        objectHelper.createObject(1569, "Single Param Object", null, bi(TYPE_A), null);
        objectHelper.createParameter(1570, bi(S_TEXT_ATTR), bi(1569));
        objectHelper.createTextValue(1571, "Initial Text Value");
        objectHelper.linkValuesToParameter(1570, 1571);
        objectHelper.createParameter(1572, bi(S_DATE_ATTR), bi(1569));
        objectHelper.createDateValue(1573, 1439492642L);
        objectHelper.linkValuesToParameter(1572, 1573);
        objectHelper.createParameter(1574, bi(M_REF_ATTR), bi(1569));
        objectHelper.createReferenceValue(1575, bi(OBJECT_B));
        objectHelper.createReferenceValue(1576, bi(OBJECT_C));
        objectHelper.linkValuesToParameter(1574, 1575, 1576);
        objectHelper.createParameter(1577, bi(M_LIST_ATTR), bi(1569));
        objectHelper.createListValue(1578, bi(M_LIST_A));
        objectHelper.createListValue(1579, bi(M_LIST_B));
        objectHelper.linkValuesToParameter(1577, 1578, 1579);

        transaction.begin();

        Attribute text = attrProvider.getById(bi(S_TEXT_ATTR));
        Attribute date = attrProvider.getById(bi(S_DATE_ATTR));
        Attribute ref = attrProvider.getById(bi(M_REF_ATTR));
        Attribute list = attrProvider.getById(bi(M_LIST_ATTR));

        DataObject dataObject = objectProvider.getById(bi(1569));
        dataObject.setSingleValue(text, null);
        dataObject.setSingleValue(date, null);
        dataObject.setSingleValue(ref, null);
        dataObject.setSingleValue(list, null);

        transaction.commit();

        Collection<BigInteger> parameters = objectHelper.readParametersByObject(bi(1569));
        Map<String, Object> textParameter = objectHelper.readParameter(bi(1570));
        Map<String, Object> dateParameter = objectHelper.readParameter(bi(1572));
        Map<String, Object> refParameter = objectHelper.readParameter(bi(1574));
        Map<String, Object> listParameter = objectHelper.readParameter(bi(1577));
        Map<String, Object> textValue = objectHelper.readValue(bi(1571));
        Map<String, Object> dateValue = objectHelper.readValue(bi(1573));
        Map<String, Object> firstRefValue = objectHelper.readValue(bi(1575));
        Map<String, Object> secondRefValue = objectHelper.readValue(bi(1576));
        Map<String, Object> firstListValue = objectHelper.readValue(bi(1578));
        Map<String, Object> secondListValue = objectHelper.readValue(bi(1579));

        assertTrue(parameters.isEmpty());

        assertNull(textParameter);
        assertNull(dateParameter);
        assertNull(refParameter);
        assertNull(listParameter);

        assertNull(textValue);
        assertNull(dateValue);
        assertNull(firstRefValue);
        assertNull(secondRefValue);
        assertNull(firstListValue);
        assertNull(secondListValue);
    }

    @Test(expected = IllegalArgumentException.class)
    @InSequence(8)
    public void testSetSingleValue_shouldCheckNullAttribute() throws Exception {
        objectHelper.createObject(1580, "Param Object", null, bi(TYPE_A), null);

        transaction.begin();

        DataObject dataObject = objectProvider.getById(bi(1580));
        dataObject.setSingleValue(null, "Undefined Value");

        transaction.commit();
    }

    @Test
    @InSequence(9)
    public void testSetMultipleValues_shouldCreateMultipleParameter() throws Exception {
        objectHelper.createObject(1581, "Multiple Param Object", null, bi(TYPE_A), null);

        transaction.begin();

        Attribute text = attrProvider.getById(bi(M_TEXT_ATTR));
        Attribute date = attrProvider.getById(bi(M_DATE_ATTR));
        Attribute ref = attrProvider.getById(bi(M_REF_ATTR));
        Attribute list = attrProvider.getById(bi(M_LIST_ATTR));
        Attribute restRef = attrProvider.getById(bi(M_R_REF_ATTR));

        DataObject dataObject = objectProvider.getById(bi(1581));
        DataObject objectA = objectProvider.getById(bi(OBJECT_A));
        DataObject objectB = objectProvider.getById(bi(OBJECT_B));

        dataObject.setMultipleValues(text, asList("Text 1", "Text 2", "Text 3", "Text 4"));
        dataObject.setMultipleValues(date, asList(new DateTime(1439409164L), new DateTime(1439493862L),
                new DateTime(1439493881L)));
        dataObject.setMultipleValues(ref, asList(objectA, objectB));
        dataObject.setMultipleValues(list, asList(emptyList()));
        dataObject.setMultipleValues(restRef, null);

        transaction.commit();

        Collection<BigInteger> parameters = objectHelper.readParametersByObject(bi(1581));

        assertEquals(3, parameters.size());
        BigInteger textParameter = null;
        BigInteger dateParameter = null;
        BigInteger refParameter = null;

        for (BigInteger parameter : parameters) {
            Map<String, Object> parameterData = objectHelper.readParameter(parameter);
            assertEquals(bi(1581), parameterData.get("object"));
            BigInteger paramAttr = (BigInteger) parameterData.get("attribute");
            BigInteger paramId = (BigInteger) parameterData.get("id");
            if (bi(M_TEXT_ATTR).equals(paramAttr)) {
                textParameter = paramId;
            } else if (bi(M_DATE_ATTR).equals(paramAttr)) {
                dateParameter = paramId;
            } else if (bi(M_REF_ATTR).equals(paramAttr)) {
                refParameter = paramId;
            } else {
                fail(paramAttr.toString());
            }
        }
        if (textParameter == null) {
            fail();
            return;
        }
        if (dateParameter == null) {
            fail();
            return;
        }
        if (refParameter == null) {
            fail();
            return;
        }

        List<BigInteger> textValues = objectHelper.readValuesByParameter(textParameter);
        List<BigInteger> dateValues = objectHelper.readValuesByParameter(dateParameter);
        List<BigInteger> refValues = objectHelper.readValuesByParameter(refParameter);

        assertEquals(4, textValues.size());
        assertEquals(3, dateValues.size());
        assertEquals(2, refValues.size());

        Map<String, Object> firstText = objectHelper.readValue(textValues.get(0));
        Map<String, Object> secondText = objectHelper.readValue(textValues.get(1));
        Map<String, Object> thirdText = objectHelper.readValue(textValues.get(2));
        Map<String, Object> fourthText = objectHelper.readValue(textValues.get(3));
        Map<String, Object> firstDate = objectHelper.readValue(dateValues.get(0));
        Map<String, Object> secondDate = objectHelper.readValue(dateValues.get(1));
        Map<String, Object> thirdDate = objectHelper.readValue(dateValues.get(2));
        Map<String, Object> firstRef = objectHelper.readValue(refValues.get(0));
        Map<String, Object> secondRef = objectHelper.readValue(refValues.get(1));

        assertEquals(0, firstText.get("type"));
        assertEquals("Text 1", firstText.get("text"));
        assertNull(firstText.get("date"));
        assertNull(firstText.get("reference"));
        assertNull(firstText.get("listEntry"));
        assertEquals(0, firstText.get("order"));

        assertEquals(0, secondText.get("type"));
        assertEquals("Text 2", secondText.get("text"));
        assertNull(secondText.get("date"));
        assertNull(secondText.get("reference"));
        assertNull(secondText.get("listEntry"));
        assertEquals(1, secondText.get("order"));

        assertEquals(0, thirdText.get("type"));
        assertEquals("Text 3", thirdText.get("text"));
        assertNull(thirdText.get("date"));
        assertNull(thirdText.get("reference"));
        assertNull(thirdText.get("listEntry"));
        assertEquals(2, thirdText.get("order"));

        assertEquals(0, fourthText.get("type"));
        assertEquals("Text 4", fourthText.get("text"));
        assertNull(fourthText.get("date"));
        assertNull(fourthText.get("reference"));
        assertNull(fourthText.get("listEntry"));
        assertEquals(3, fourthText.get("order"));

        assertEquals(1, firstDate.get("type"));
        assertNull(firstDate.get("text"));
        assertEquals(bi(1439409164), firstDate.get("date"));
        assertNull(firstDate.get("reference"));
        assertNull(firstDate.get("listEntry"));
        assertEquals(0, firstDate.get("order"));

        assertEquals(1, secondDate.get("type"));
        assertNull(secondDate.get("text"));
        assertEquals(bi(1439493862), secondDate.get("date"));
        assertNull(secondDate.get("reference"));
        assertNull(secondDate.get("listEntry"));
        assertEquals(1, secondDate.get("order"));

        assertEquals(1, thirdDate.get("type"));
        assertNull(thirdDate.get("text"));
        assertEquals(bi(1439493881), thirdDate.get("date"));
        assertNull(thirdDate.get("reference"));
        assertNull(thirdDate.get("listEntry"));
        assertEquals(2, thirdDate.get("order"));

        assertEquals(2, firstRef.get("type"));
        assertNull(firstRef.get("text"));
        assertNull(firstRef.get("date"));
        assertEquals(bi(OBJECT_A), firstRef.get("reference"));
        assertNull(firstRef.get("listEntry"));
        assertEquals(0, firstRef.get("order"));

        assertEquals(2, secondRef.get("type"));
        assertNull(secondRef.get("text"));
        assertNull(secondRef.get("date"));
        assertEquals(bi(OBJECT_B), secondRef.get("reference"));
        assertNull(secondRef.get("listEntry"));
        assertEquals(1, secondRef.get("order"));
    }

    @Test
    @InSequence(10)
    public void testSetMultipleValues_shouldUpdateParametersSet() throws Exception {
        objectHelper.createObject(1582, "Multiple Param Object", null, bi(TYPE_A), null);
        objectHelper.createParameter(1583, bi(M_TEXT_ATTR), bi(1582));
        objectHelper.createTextValue(1584, "Old Text");
        objectHelper.linkValuesToParameter(1583, 1584);
        objectHelper.createParameter(1585, bi(M_DATE_ATTR), bi(1582));
        objectHelper.createDateValue(1586, 1439495307L);
        objectHelper.linkValuesToParameter(1585, 1586);
        objectHelper.createParameter(1587, bi(M_REF_ATTR), bi(1582));
        objectHelper.createReferenceValue(1588, bi(OBJECT_A));
        objectHelper.createReferenceValue(1589, bi(OBJECT_B));
        objectHelper.linkValuesToParameter(1587, 1588, 1589);
        objectHelper.createParameter(1590, bi(M_LIST_ATTR), bi(1582));
        objectHelper.createListValue(1591, bi(M_LIST_A));
        objectHelper.createListValue(1592, bi(M_LIST_B));
        objectHelper.createListValue(1593, bi(M_LIST_A));
        objectHelper.linkValuesToParameter(1590, 1591, 1592, 1593);

        transaction.begin();

        Attribute text = attrProvider.getById(bi(M_TEXT_ATTR));
        Attribute date = attrProvider.getById(bi(M_DATE_ATTR));
        Attribute ref = attrProvider.getById(bi(M_REF_ATTR));
        Attribute list = attrProvider.getById(bi(M_LIST_ATTR));
        ListEntry entryC = listProvider.getById(bi(M_LIST_C));
        DataObject objectA = objectProvider.getById(bi(OBJECT_A));
        DataObject objectB = objectProvider.getById(bi(OBJECT_B));

        DataObject dataObject = objectProvider.getById(bi(1582));
        dataObject.setMultipleValues(text, asList("New Text 1", "New Text 2"));
        dataObject.setMultipleValues(date, asList(new DateTime(1439496406),
                new DateTime(1439496407), new DateTime(1439496408)));
        dataObject.setMultipleValues(ref, asList(objectB, objectA));
        dataObject.setMultipleValues(list, asList(entryC));

        transaction.commit();

        Collection<BigInteger> parameters = objectHelper.readParametersByObject(bi(1582));
        Map<String, Object> textParameter = objectHelper.readParameter(bi(1583));
        Map<String, Object> dateParameter = objectHelper.readParameter(bi(1585));
        Map<String, Object> refParameter = objectHelper.readParameter(bi(1587));
        Map<String, Object> listParameter = objectHelper.readParameter(bi(1590));
        List<BigInteger> textValues = objectHelper.readValuesByParameter(bi(1583));
        List<BigInteger> dateValues = objectHelper.readValuesByParameter(bi(1585));
        List<BigInteger> refValues = objectHelper.readValuesByParameter(bi(1587));
        List<BigInteger> listValues = objectHelper.readValuesByParameter(bi(1590));

        assertEquals(4, parameters.size());
        assertTrue(parameters.contains(bi(1583)));
        assertTrue(parameters.contains(bi(1585)));
        assertTrue(parameters.contains(bi(1587)));
        assertTrue(parameters.contains(bi(1590)));

        assertEquals(2, textValues.size());
        assertEquals(3, dateValues.size());
        assertEquals(2, refValues.size());
        assertEquals(1, listValues.size());

        assertEquals(bi(1584), textValues.get(0));
        assertEquals(bi(1586), dateValues.get(0));
        assertEquals(bi(1588), refValues.get(0));
        assertEquals(bi(1589), refValues.get(1));
        assertEquals(bi(1591), listValues.get(0));

        assertNull(objectHelper.readValue(bi(1592)));
        assertNull(objectHelper.readValue(bi(1593)));

        assertEquals(bi(M_TEXT_ATTR), textParameter.get("attribute"));
        assertEquals(bi(M_DATE_ATTR), dateParameter.get("attribute"));
        assertEquals(bi(M_REF_ATTR), refParameter.get("attribute"));
        assertEquals(bi(M_LIST_ATTR), listParameter.get("attribute"));

        Map<String, Object> firstText = objectHelper.readValue(textValues.get(0));
        Map<String, Object> secondText = objectHelper.readValue(textValues.get(1));
        Map<String, Object> firstDate = objectHelper.readValue(dateValues.get(0));
        Map<String, Object> secondDate = objectHelper.readValue(dateValues.get(1));
        Map<String, Object> thirdDate = objectHelper.readValue(dateValues.get(2));
        Map<String, Object> firstRef = objectHelper.readValue(refValues.get(0));
        Map<String, Object> secondRef = objectHelper.readValue(refValues.get(1));
        Map<String, Object> firstList = objectHelper.readValue(listValues.get(0));

        assertEquals(0, firstText.get("type"));
        assertEquals("New Text 1", firstText.get("text"));
        assertNull(firstText.get("date"));
        assertNull(firstText.get("reference"));
        assertNull(firstText.get("listEntry"));
        assertEquals(0, firstText.get("order"));

        assertEquals(0, secondText.get("type"));
        assertEquals("New Text 2", secondText.get("text"));
        assertNull(secondText.get("date"));
        assertNull(secondText.get("reference"));
        assertNull(secondText.get("listEntry"));
        assertEquals(1, secondText.get("order"));

        assertEquals(1, firstDate.get("type"));
        assertNull(firstDate.get("text"));
        assertEquals(bi(1439496406), firstDate.get("date"));
        assertNull(firstDate.get("reference"));
        assertNull(firstDate.get("listEntry"));
        assertEquals(0, firstDate.get("order"));

        assertEquals(1, secondDate.get("type"));
        assertNull(secondDate.get("text"));
        assertEquals(bi(1439496407), secondDate.get("date"));
        assertNull(secondDate.get("reference"));
        assertNull(secondDate.get("listEntry"));
        assertEquals(1, secondDate.get("order"));

        assertEquals(1, thirdDate.get("type"));
        assertNull(thirdDate.get("text"));
        assertEquals(bi(1439496408), thirdDate.get("date"));
        assertNull(thirdDate.get("reference"));
        assertNull(thirdDate.get("listEntry"));
        assertEquals(2, thirdDate.get("order"));

        assertEquals(2, firstRef.get("type"));
        assertNull(firstRef.get("text"));
        assertNull(firstRef.get("date"));
        assertEquals(bi(OBJECT_B), firstRef.get("reference"));
        assertNull(firstRef.get("listEntry"));
        assertEquals(0, firstRef.get("order"));

        assertEquals(2, secondRef.get("type"));
        assertNull(secondRef.get("text"));
        assertNull(secondRef.get("date"));
        assertEquals(bi(OBJECT_A), secondRef.get("reference"));
        assertNull(secondRef.get("listEntry"));
        assertEquals(1, secondRef.get("order"));

        assertEquals(3, firstList.get("type"));
        assertNull(firstList.get("text"));
        assertNull(firstList.get("date"));
        assertNull(firstList.get("reference"));
        assertEquals(bi(M_LIST_C), firstList.get("listEntry"));
        assertEquals(0, firstList.get("order"));
    }

    @Test
    @InSequence(11)
    public void testSetMultipleValues_shouldRemoveMultipleParameter() throws Exception {
        objectHelper.createObject(1594, "Multiple Param Object", null, bi(TYPE_A), null);
        objectHelper.createParameter(1595, bi(M_TEXT_ATTR), bi(1594));
        objectHelper.createTextValue(1596, "Old Text");
        objectHelper.linkValuesToParameter(1595, 1596);
        objectHelper.createParameter(1597, bi(M_DATE_ATTR), bi(1594));
        objectHelper.createDateValue(1598, 1439495307L);
        objectHelper.linkValuesToParameter(1597, 1598);
        objectHelper.createParameter(1599, bi(M_REF_ATTR), bi(1594));
        objectHelper.createReferenceValue(1600, bi(OBJECT_A));
        objectHelper.createReferenceValue(1601, bi(OBJECT_B));
        objectHelper.linkValuesToParameter(1599, 1600, 1601);
        objectHelper.createParameter(1602, bi(M_LIST_ATTR), bi(1594));
        objectHelper.createListValue(1603, bi(M_LIST_A));
        objectHelper.createListValue(1604, bi(M_LIST_B));
        objectHelper.createListValue(1605, bi(M_LIST_A));
        objectHelper.linkValuesToParameter(1602, 1603, 1604, 1605);

        transaction.begin();

        Attribute text = attrProvider.getById(bi(M_TEXT_ATTR));
        Attribute date = attrProvider.getById(bi(M_DATE_ATTR));
        Attribute ref = attrProvider.getById(bi(M_REF_ATTR));
        Attribute list = attrProvider.getById(bi(M_LIST_ATTR));

        DataObject dataObject = objectProvider.getById(bi(1594));
        dataObject.setMultipleValues(text, new ArrayList<>());
        dataObject.setMultipleValues(date, null);
        dataObject.setMultipleValues(ref, emptyList());
        dataObject.setMultipleValues(list, null);

        transaction.commit();

        Collection<BigInteger> parameters = objectHelper.readParametersByObject(bi(1594));
        Map<String, Object> textParameter = objectHelper.readParameter(bi(1595));
        Map<String, Object> dateParameter = objectHelper.readParameter(bi(1597));
        Map<String, Object> refParameter = objectHelper.readParameter(bi(1599));
        Map<String, Object> listParameter = objectHelper.readParameter(bi(1602));
        Map<String, Object> firstText = objectHelper.readValue(bi(1596));
        Map<String, Object> firstDate = objectHelper.readValue(bi(1598));
        Map<String, Object> firstRef = objectHelper.readValue(bi(1600));
        Map<String, Object> secondRef = objectHelper.readValue(bi(1601));
        Map<String, Object> firstList = objectHelper.readValue(bi(1603));
        Map<String, Object> secondList = objectHelper.readValue(bi(1604));
        Map<String, Object> thirdList = objectHelper.readValue(bi(1605));

        assertTrue(parameters.isEmpty());
        assertNull(textParameter);
        assertNull(dateParameter);
        assertNull(refParameter);
        assertNull(listParameter);
        assertNull(firstText);
        assertNull(firstDate);
        assertNull(firstRef);
        assertNull(secondRef);
        assertNull(firstList);
        assertNull(secondList);
        assertNull(thirdList);
    }

    @Test(expected = IllegalArgumentException.class)
    @InSequence(12)
    public void testSetMultipleValues_shouldCheckNullAttribute() throws Exception {
        objectHelper.createObject(1606, "Multiple param object", null, bi(TYPE_A), null);

        transaction.begin();

        DataObject object = objectProvider.getById(bi(1606));
        object.setMultipleValues(null, emptyList());

        transaction.commit();
    }

    @Test(expected = IllegalArgumentException.class)
    @InSequence(13)
    public void testSetMultipleValues_shouldCheckSingleAttribute() throws Exception {
        objectHelper.createObject(1607, "Multiple param object", null, bi(TYPE_A), null);

        transaction.begin();

        Attribute singleAttribute = attrProvider.getById(bi(S_TEXT_ATTR));
        DataObject object = objectProvider.getById(bi(1607));
        object.setMultipleValues(singleAttribute, emptyList());

        transaction.commit();
    }

    @Test
    @InSequence(14)
    public void testAddMultipleValue_shouldUpdateMultipleParameter() {

    }

    @Test(expected = IllegalArgumentException.class)
    @InSequence(15)
    public void testAddMultipleValue_shouldCheckNullAttribute() throws Exception {
        objectHelper.createObject(N, "Multiple param object", null, bi(TYPE_A), null);

        transaction.begin();

        DataObject object = objectProvider.getById(bi(N));
        object.addMultipleValue(null, "Value");

        transaction.commit();
    }

    @Test(expected = IllegalArgumentException.class)
    @InSequence(16)
    public void testAddMultipleValue_shouldCheckSingleAttribute() throws Exception {
        objectHelper.createObject(N, "Multiple param object", null, bi(TYPE_A), null);

        transaction.begin();

        Attribute singleAttribute = attrProvider.getById(bi(S_DATE_ATTR));
        DataObject object = objectProvider.getById(bi(N));
        object.addMultipleValue(singleAttribute, new DateTime(1439499213));

        transaction.commit();
    }

    @Test(expected = IllegalArgumentException.class)
    @InSequence(17)
    public void testAddMultipleValue_shouldCheckNullValue() throws Exception {
        objectHelper.createObject(N, "Multiple param object", null, bi(TYPE_A), null);

        transaction.begin();

        Attribute attribute = attrProvider.getById(bi(M_REF_ATTR));
        DataObject object = objectProvider.getById(bi(N));
        object.addMultipleValue(attribute, null);

        transaction.commit();
    }

    @Test
    @InSequence(18)
    public void testUpdate_shouldCheckInvalidListEntries() {

    }

    @Test
    @InSequence(19)
    public void testUpdate_shouldCheckInvalidReferenceTypes() {

    }

    @Test
    @InSequence(20)
    public void testUpdate_shouldRollbackUpdate() {

    }

    @Test
    @InSequence(21)
    public void testRemove_shouldRemoveParameters() {

    }

    @Test
    @InSequence(22)
    public void testRemove_shouldRollbackRemove() {

    }
}
