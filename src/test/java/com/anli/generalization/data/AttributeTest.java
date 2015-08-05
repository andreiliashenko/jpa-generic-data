package com.anli.generalization.data;

import com.anli.generalization.data.access.metadata.AttributeProvider;
import com.anli.generalization.data.access.metadata.ListEntryProvider;
import com.anli.generalization.data.access.metadata.ObjectTypeProvider;
import com.anli.generalization.data.entities.metadata.Attribute;
import com.anli.generalization.data.entities.metadata.AttributeType;
import com.anli.generalization.data.entities.metadata.ListEntry;
import com.anli.generalization.data.entities.metadata.ObjectType;
import com.anli.generalization.data.factory.JpaProviderFactory;
import com.anli.generalization.data.utils.AttributeHelper;
import com.anli.generalization.data.utils.ListEntryHelper;
import com.anli.generalization.data.utils.ObjectTypeHelper;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.transaction.IllegalTransactionStateException;

import static com.anli.generalization.data.entities.metadata.AttributeType.DATE;
import static com.anli.generalization.data.entities.metadata.AttributeType.LIST;
import static com.anli.generalization.data.entities.metadata.AttributeType.REFERENCE;
import static com.anli.generalization.data.entities.metadata.AttributeType.TEXT;
import static com.anli.generalization.data.utils.CommonDeployment.getDeployment;
import static com.anli.generalization.data.utils.ValueFactory.bi;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class AttributeTest {

    @Deployment
    public static Archive createDeployment() {
        return getDeployment();
    }

    @Resource(lookup = "java:/jdbc/integration_testing")
    private DataSource dataSource;

    @Resource
    private UserTransaction transaction;

    private ObjectTypeHelper objectTypeHelper;
    private ListEntryHelper listEntryHelper;
    private AttributeHelper attributeHelper;

    private ObjectTypeProvider objectTypeProvider;
    private ListEntryProvider listEntryProvider;
    private AttributeProvider attributeProvider;

    @Before
    public void setUp() {
        objectTypeHelper = new ObjectTypeHelper(dataSource);
        listEntryHelper = new ListEntryHelper(dataSource);
        attributeHelper = new AttributeHelper(dataSource);
        objectTypeProvider = JpaProviderFactory.getInstance().getObjectTypeProvider();
        listEntryProvider = JpaProviderFactory.getInstance().getListEntryProvider();
        attributeProvider = JpaProviderFactory.getInstance().getAttributeProvider();
    }

    @Test
    @InSequence(0)
    public void testCreate_shouldCreateEmptyWithKey() throws Exception {
        transaction.begin();

        Attribute attribute = attributeProvider.create();
        BigInteger id = attribute.getId();

        transaction.commit();

        assertNotNull(id);

        Map<String, Object> attrData = attributeHelper.readAttribute(id);

        assertEquals(id, attrData.get("id"));
        assertNull(attrData.get("type"));
        assertNull(attrData.get("name"));
        assertNull(attrData.get("multiple"));
        assertNull(attrData.get("referenceType"));
        assertNull(attrData.get("objectType"));
        assertNull(attrData.get("order"));
    }

    @Test
    @InSequence(1)
    public void testCreate_shouldCreateWithData() throws Exception {
        listEntryHelper.createListEntry(1201, "Entry A");
        listEntryHelper.createListEntry(1202, "Entry B");
        objectTypeHelper.createObjectType(1203, "Ref type");

        transaction.begin();

        ListEntry entryA = listEntryProvider.getById(bi(1201));
        ListEntry entryB = listEntryProvider.getById(bi(1202));
        ObjectType refType = objectTypeProvider.getById(bi(1203));
        Attribute attribute = attributeProvider.create();
        BigInteger id = attribute.getId();
        attribute.setType(DATE);
        attribute.setName("Created Name");
        attribute.setMultiple(true);
        attribute.setReferenceType(refType);
        attribute.getListEntries().add(entryA);
        attribute.getListEntries().add(entryB);

        transaction.commit();

        assertNotNull(id);

        Map<String, Object> attrData = attributeHelper.readAttribute(id);

        assertEquals(id, attrData.get("id"));
        assertEquals(1, attrData.get("type"));
        assertEquals("Created Name", attrData.get("name"));
        assertEquals(1, attrData.get("multiple"));
        assertEquals(bi(1203), attrData.get("referenceType"));
        assertNull(attrData.get("objectType"));
        assertNull(attrData.get("order"));

        Map<String, Object> entryAData = listEntryHelper.readListEntry(bi(1201));
        Map<String, Object> entryBData = listEntryHelper.readListEntry(bi(1202));
        List<BigInteger> attributeEntries = listEntryHelper.readEntriesByAttribute(id);

        assertEquals(id, entryAData.get("attribute"));
        assertEquals(0, entryAData.get("order"));
        assertEquals(id, entryBData.get("attribute"));
        assertEquals(1, entryBData.get("order"));
        assertEquals(2, attributeEntries.size());
        assertEquals(bi(1201), attributeEntries.get(0));
        assertEquals(bi(1202), attributeEntries.get(1));
    }

    @Test
    @InSequence(2)
    public void testCreate_shouldRollbackCreation() throws Exception {
        listEntryHelper.createListEntry(1204, "Entry A");
        listEntryHelper.createListEntry(1205, "Entry B");
        objectTypeHelper.createObjectType(1206, "Ref type");

        transaction.begin();

        ListEntry entryA = listEntryProvider.getById(bi(1204));
        ListEntry entryB = listEntryProvider.getById(bi(1205));
        ObjectType refType = objectTypeProvider.getById(bi(1206));
        Attribute attribute = attributeProvider.create();
        BigInteger id = attribute.getId();
        attribute.setType(TEXT);
        attribute.setName("Created Name");
        attribute.setMultiple(false);
        attribute.setReferenceType(refType);
        attribute.getListEntries().add(entryA);
        attribute.getListEntries().add(entryB);

        transaction.rollback();

        assertNotNull(id);

        Map<String, Object> attrData = attributeHelper.readAttribute(id);

        assertNull(attrData);

        Map<String, Object> entryAData = listEntryHelper.readListEntry(bi(1204));
        Map<String, Object> entryBData = listEntryHelper.readListEntry(bi(1205));
        List<BigInteger> attributeEntries = listEntryHelper.readEntriesByAttribute(id);

        assertNull(entryAData.get("attribute"));
        assertNull(entryAData.get("order"));
        assertNull(entryBData.get("attribute"));
        assertNull(entryBData.get("order"));
        assertTrue(attributeEntries.isEmpty());
    }

    @Test(expected = IllegalTransactionStateException.class)
    @InSequence(3)
    public void testCreation_shouldForbidNonTransactionalCall() {
        attributeProvider.create();
    }

    @Test
    @InSequence(4)
    public void testReading_shouldReadWithoutEdit() throws Exception {
        listEntryHelper.createListEntry(1207, "Entry A");
        listEntryHelper.createListEntry(1208, "Entry B");
        objectTypeHelper.createObjectType(1209, "Ref type");
        attributeHelper.createAttribute(1210, REFERENCE, "Read name", null, bi(1209));
        listEntryHelper.linkListEntriesToAttribute(1210, 1207, 1208);

        transaction.begin();

        Attribute attribute = attributeProvider.getById(bi(1210));
        BigInteger id = attribute.getId();
        AttributeType type = attribute.getType();
        String name = attribute.getName();
        boolean multiple = attribute.isMultiple();
        ObjectType refType = attribute.getReferenceType();
        List<ListEntry> entries = attribute.getListEntries();

        assertEquals(2, entries.size());

        ListEntry entryA = entries.get(0);
        ListEntry entryB = entries.get(1);
        BigInteger refTypeId = refType.getId();
        String refTypeName = refType.getName();
        BigInteger entryAId = entryA.getId();
        String entryAValue = entryA.getEntryValue();
        BigInteger entryBId = entryB.getId();
        String entryBValue = entryB.getEntryValue();

        transaction.commit();

        assertEquals(bi(1210), id);
        assertEquals(REFERENCE, type);
        assertEquals("Read name", name);
        assertFalse(multiple);
        assertEquals(bi(1209), refTypeId);
        assertEquals("Ref type", refTypeName);
        assertEquals(bi(1207), entryAId);
        assertEquals("Entry A", entryAValue);
        assertEquals(bi(1208), entryBId);
        assertEquals("Entry B", entryBValue);

        Map<String, Object> attrData = attributeHelper.readAttribute(bi(1210));

        assertEquals(bi(1210), attrData.get("id"));
        assertEquals(2, attrData.get("type"));
        assertEquals("Read name", attrData.get("name"));
        assertEquals(null, attrData.get("multiple"));
        assertEquals(bi(1209), attrData.get("referenceType"));
        assertNull(attrData.get("objectType"));
        assertNull(attrData.get("order"));

        Map<String, Object> entryAData = listEntryHelper.readListEntry(bi(1207));
        Map<String, Object> entryBData = listEntryHelper.readListEntry(bi(1208));
        List<BigInteger> attributeEntries = listEntryHelper.readEntriesByAttribute(bi(1210));

        assertEquals(bi(1210), entryAData.get("attribute"));
        assertEquals(0, entryAData.get("order"));
        assertEquals(bi(1210), entryBData.get("attribute"));
        assertEquals(1, entryBData.get("order"));
        assertEquals(2, attributeEntries.size());
        assertEquals(bi(1207), attributeEntries.get(0));
        assertEquals(bi(1208), attributeEntries.get(1));
    }

    @Test
    @InSequence(5)
    public void testReading_shouldReadNull() throws Exception {
        transaction.begin();

        Attribute attribute = attributeProvider.getById(bi(1211));

        transaction.commit();

        assertNull(attribute);
    }

    @Test(expected = IllegalTransactionStateException.class)
    @InSequence(6)
    public void testReading_shouldForbidNonTransactionalCall() {
        attributeHelper.createAttribute(1212, TEXT, "Attribute", true, null);
        attributeProvider.getById(bi(1212));
    }

    @Test(expected = IllegalArgumentException.class)
    @InSequence(7)
    public void testReading_shouldCheckNullId() throws Exception {
        transaction.begin();

        attributeProvider.getById(null);

        transaction.commit();
    }

    @Test
    @InSequence(8)
    public void testUpdate_shouldUpdateData() throws Exception {
        objectTypeHelper.createObjectType(1213, "Ref type");
        listEntryHelper.createListEntry(1214, "Entry A");
        listEntryHelper.createListEntry(1215, "Entry B");
        listEntryHelper.createListEntry(1216, "Entry C");
        attributeHelper.createAttribute(1217, LIST, "Initial name", false, bi(1213));
        listEntryHelper.linkListEntriesToAttribute(1217, 1214, 1215);

        transaction.begin();

        Attribute attribute = attributeProvider.getById(bi(1217));
        attribute.setType(TEXT);
        attribute.setName("Updated name");
        attribute.setMultiple(true);
        attribute.setReferenceType(null);
        List<ListEntry> listEntries = attribute.getListEntries();
        ListEntry entryC = listEntryProvider.getById(bi(1216));
        listEntries.remove(0);
        listEntries.add(entryC);

        transaction.commit();

        Map<String, Object> attrData = attributeHelper.readAttribute(bi(1217));

        assertEquals(bi(1217), attrData.get("id"));
        assertEquals(0, attrData.get("type"));
        assertEquals("Updated name", attrData.get("name"));
        assertEquals(1, attrData.get("multiple"));
        assertNull(attrData.get("referenceType"));
        assertNull(attrData.get("objectType"));
        assertNull(attrData.get("order"));

        Map<String, Object> entryAData = listEntryHelper.readListEntry(bi(1214));
        Map<String, Object> entryBData = listEntryHelper.readListEntry(bi(1215));
        Map<String, Object> entryCData = listEntryHelper.readListEntry(bi(1216));
        List<BigInteger> attributeEntries = listEntryHelper.readEntriesByAttribute(bi(1217));

        assertNull(entryAData.get("attribute"));
        assertNull(entryAData.get("order"));
        assertEquals(bi(1217), entryBData.get("attribute"));
        assertEquals(0, entryBData.get("order"));
        assertEquals(bi(1217), entryCData.get("attribute"));
        assertEquals(1, entryCData.get("order"));
        assertEquals(2, attributeEntries.size());
        assertEquals(bi(1215), attributeEntries.get(0));
        assertEquals(bi(1216), attributeEntries.get(1));
    }

    @Test
    @InSequence(9)
    public void testUpdate_shouldRollbackUpdate() throws Exception {
        objectTypeHelper.createObjectType(1218, "Ref type");
        listEntryHelper.createListEntry(1219, "Entry A");
        listEntryHelper.createListEntry(1220, "Entry B");
        listEntryHelper.createListEntry(1221, "Entry C");
        attributeHelper.createAttribute(1222, LIST, "Initial name", false, bi(1218));
        listEntryHelper.linkListEntriesToAttribute(1222, 1219, 1220);

        transaction.begin();

        Attribute attribute = attributeProvider.getById(bi(1222));
        attribute.setType(TEXT);
        attribute.setName("Updated name");
        attribute.setMultiple(true);
        attribute.setReferenceType(null);
        List<ListEntry> listEntries = attribute.getListEntries();
        ListEntry entryC = listEntryProvider.getById(bi(1221));
        listEntries.remove(0);
        listEntries.add(entryC);

        transaction.rollback();

        Map<String, Object> attrData = attributeHelper.readAttribute(bi(1222));

        assertEquals(bi(1222), attrData.get("id"));
        assertEquals(3, attrData.get("type"));
        assertEquals("Initial name", attrData.get("name"));
        assertEquals(0, attrData.get("multiple"));
        assertEquals(bi(1218), attrData.get("referenceType"));
        assertNull(attrData.get("objectType"));
        assertNull(attrData.get("order"));

        Map<String, Object> entryAData = listEntryHelper.readListEntry(bi(1219));
        Map<String, Object> entryBData = listEntryHelper.readListEntry(bi(1220));
        Map<String, Object> entryCData = listEntryHelper.readListEntry(bi(1221));
        List<BigInteger> attributeEntries = listEntryHelper.readEntriesByAttribute(bi(1222));

        assertEquals(bi(1222), entryAData.get("attribute"));
        assertEquals(0, entryAData.get("order"));
        assertEquals(bi(1222), entryBData.get("attribute"));
        assertEquals(1, entryBData.get("order"));
        assertNull(entryCData.get("attribute"));
        assertNull(entryCData.get("order"));
        assertEquals(2, attributeEntries.size());
        assertEquals(bi(1219), attributeEntries.get(0));
        assertEquals(bi(1220), attributeEntries.get(1));
    }

    @Test
    @InSequence(10)
    public void testRemove_shouldRemoveData() throws Exception {
        objectTypeHelper.createObjectType(1223, "Ref type");
        listEntryHelper.createListEntry(1224, "Single Entry");
        attributeHelper.createAttribute(1225, DATE, "Removed name", true, bi(1223));
        listEntryHelper.linkListEntriesToAttribute(1225, 1224);

        transaction.begin();

        Attribute attribute = attributeProvider.getById(bi(1225));
        attributeProvider.remove(attribute);

        transaction.commit();

        Map<String, Object> attributeData = attributeHelper.readAttribute(bi(1225));

        assertNull(attributeData);

        Map<String, Object> refTypeData = objectTypeHelper.readObjectType(bi(1223));

        assertNotNull(refTypeData);

        Map<String, Object> entryData = listEntryHelper.readListEntry(bi(1224));
        List<BigInteger> entries = listEntryHelper.readEntriesByAttribute(bi(1225));

        assertNull(entryData.get("attribute"));
        assertNull(entryData.get("order"));
        assertTrue(entries.isEmpty());
    }

    @Test
    @InSequence(12)
    public void testRemove_shouldRollbackRemove() throws Exception {
        objectTypeHelper.createObjectType(1226, "Ref type");
        listEntryHelper.createListEntry(1227, "Single Entry");
        attributeHelper.createAttribute(1228, DATE, "Rollback name", true, bi(1226));
        listEntryHelper.linkListEntriesToAttribute(1228, 1227);

        transaction.begin();

        Attribute attribute = attributeProvider.getById(bi(1228));
        attributeProvider.remove(attribute);

        transaction.rollback();

        Map<String, Object> attributeData = attributeHelper.readAttribute(bi(1228));

        assertNotNull(attributeData);
        assertEquals(bi(1228), attributeData.get("id"));
        assertEquals(1, attributeData.get("type"));
        assertEquals("Rollback name", attributeData.get("name"));
        assertEquals(1, attributeData.get("multiple"));
        assertEquals(bi(1226), attributeData.get("referenceType"));
        assertNull(attributeData.get("objectType"));
        assertNull(attributeData.get("order"));

        Map<String, Object> entryData = listEntryHelper.readListEntry(bi(1227));
        List<BigInteger> entries = listEntryHelper.readEntriesByAttribute(bi(1228));

        assertEquals(bi(1228), entryData.get("attribute"));
        assertEquals(0, entryData.get("order"));
        assertEquals(1, entries.size());
        assertEquals(bi(1227), entries.iterator().next());
    }

    @Test(expected = IllegalTransactionStateException.class)
    @InSequence(13)
    public void testRemove_shouldForbidNonTransactionalCall() throws Exception {
        attributeHelper.createAttribute(1229, null, "Non-removed name", null, null);

        transaction.begin();

        Attribute attribute = attributeProvider.getById(bi(1229));

        transaction.commit();

        attributeProvider.remove(attribute);
    }

    @Test(expected = IllegalArgumentException.class)
    @InSequence(14)
    public void testRemove_shouldCheckNull() throws Exception {
        transaction.begin();

        attributeProvider.remove(null);

        transaction.commit();
    }
}
