package com.anli.generalization.data;

import com.anli.generalization.data.access.metadata.AttributeProvider;
import com.anli.generalization.data.access.metadata.ObjectTypeProvider;
import com.anli.generalization.data.entities.metadata.Attribute;
import com.anli.generalization.data.entities.metadata.AttributeType;
import com.anli.generalization.data.entities.metadata.ObjectType;
import com.anli.generalization.data.factory.JpaProviderFactory;
import com.anli.generalization.data.utils.AttributeHelper;
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

import static com.anli.generalization.data.entities.metadata.AttributeType.DATE;
import static com.anli.generalization.data.entities.metadata.AttributeType.LIST;
import static com.anli.generalization.data.entities.metadata.AttributeType.REFERENCE;
import static com.anli.generalization.data.entities.metadata.AttributeType.TEXT;
import static com.anli.generalization.data.utils.CommonDeployment.getDeployment;
import static com.anli.generalization.data.utils.JndiUtils.getDataSource;
import static com.anli.generalization.data.utils.JndiUtils.getTransaction;
import static com.anli.generalization.data.utils.ValueFactory.bi;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class ObjectTypeAttributeTest {

    @Deployment
    public static Archive createDeployment() {
        return getDeployment();
    }

    private DataSource dataSource;

    private UserTransaction transaction;

    private ObjectTypeHelper typeHelper;
    private AttributeHelper attrHelper;

    private ObjectTypeProvider typeProvider;
    private AttributeProvider attrProvider;

    @Before
    public void setUp() {
        dataSource = getDataSource();
        transaction = getTransaction();
        typeHelper = new ObjectTypeHelper(dataSource);
        attrHelper = new AttributeHelper(dataSource);
        typeProvider = JpaProviderFactory.getInstance().getObjectTypeProvider();
        attrProvider = JpaProviderFactory.getInstance().getAttributeProvider();
    }

    @Test
    @InSequence(0)
    public void testCreate_shouldCreateWithAttributes() throws Exception {
        attrHelper.createAttribute(1301, TEXT, "Attr A", true, null);
        attrHelper.createAttribute(1302, REFERENCE, "Attr B", false, null);
        attrHelper.createAttribute(1303, DATE, "Attr C", null, null);

        transaction.begin();

        Attribute attrA = attrProvider.getById(bi(1301));
        Attribute attrB = attrProvider.getById(bi(1302));
        Attribute attrC = attrProvider.getById(bi(1303));
        ObjectType type = typeProvider.create(null);
        BigInteger id = type.getId();
        List<Attribute> attributes = type.getAttributes();
        attributes.add(attrC);
        attributes.add(attrB);
        attributes.add(attrA);

        transaction.commit();

        Map<String, Object> attrAData = attrHelper.readAttribute(bi(1301));
        Map<String, Object> attrBData = attrHelper.readAttribute(bi(1302));
        Map<String, Object> attrCData = attrHelper.readAttribute(bi(1303));
        List<BigInteger> attrIds = attrHelper.readAttributesByObjectType(id);

        assertEquals(id, attrAData.get("objectType"));
        assertEquals(2, attrAData.get("order"));
        assertEquals(id, attrBData.get("objectType"));
        assertEquals(1, attrBData.get("order"));
        assertEquals(id, attrCData.get("objectType"));
        assertEquals(0, attrCData.get("order"));
        assertEquals(3, attrIds.size());
        assertEquals(bi(1303), attrIds.get(0));
        assertEquals(bi(1302), attrIds.get(1));
        assertEquals(bi(1301), attrIds.get(2));
    }

    @Test
    @InSequence(1)
    public void testCreate_shouldRollbackCreation() throws Exception {
        attrHelper.createAttribute(1304, TEXT, "Attr A", true, null);
        attrHelper.createAttribute(1305, REFERENCE, "Attr B", false, null);
        attrHelper.createAttribute(1306, DATE, "Attr C", null, null);

        transaction.begin();

        Attribute attrA = attrProvider.getById(bi(1304));
        Attribute attrB = attrProvider.getById(bi(1305));
        Attribute attrC = attrProvider.getById(bi(1306));
        ObjectType type = typeProvider.create(null);
        BigInteger id = type.getId();
        List<Attribute> attributes = type.getAttributes();
        attributes.add(attrC);
        attributes.add(attrB);
        attributes.add(attrA);

        transaction.rollback();

        Map<String, Object> attrAData = attrHelper.readAttribute(bi(1304));
        Map<String, Object> attrBData = attrHelper.readAttribute(bi(1305));
        Map<String, Object> attrCData = attrHelper.readAttribute(bi(1306));
        List<BigInteger> attrIds = attrHelper.readAttributesByObjectType(id);

        assertNull(attrAData.get("objectType"));
        assertNull(attrAData.get("order"));
        assertNull(attrBData.get("objectType"));
        assertNull(attrBData.get("order"));
        assertNull(attrCData.get("objectType"));
        assertNull(attrCData.get("order"));
        assertTrue(attrIds.isEmpty());
    }

    @Test
    @InSequence(2)
    public void testReading_shouldReadAttributes() throws Exception {
        typeHelper.createObjectType(1307, "Type");
        attrHelper.createAttribute(1308, TEXT, "Attr A", true, null);
        attrHelper.createAttribute(1309, REFERENCE, "Attr B", false, bi(1307));
        attrHelper.createAttribute(1310, DATE, "Attr C", null, null);
        attrHelper.linkAttributesToObjectType(1307, 1310, 1309, 1308);

        transaction.begin();

        ObjectType type = typeProvider.getById(bi(1307));
        List<Attribute> attributes = type.getAttributes();

        assertEquals(3, attributes.size());

        Attribute attrC = attributes.get(0);
        BigInteger attrCId = attrC.getId();
        AttributeType attrCType = attrC.getType();
        String attrCName = attrC.getName();
        boolean attrCMultiple = attrC.isMultiple();
        Attribute attrB = attributes.get(1);
        BigInteger attrBId = attrB.getId();
        AttributeType attrBType = attrB.getType();
        String attrBName = attrB.getName();
        boolean attrBMultiple = attrB.isMultiple();
        Attribute attrA = attributes.get(2);
        BigInteger attrAId = attrA.getId();
        AttributeType attrAType = attrA.getType();
        String attrAName = attrA.getName();
        boolean attrAMultiple = attrA.isMultiple();
        ObjectType attrBRefType = attrB.getReferenceType();

        assertSame(type, attrBRefType);

        transaction.commit();

        assertEquals(bi(1308), attrAId);
        assertEquals(TEXT, attrAType);
        assertEquals("Attr A", attrAName);
        assertEquals(true, attrAMultiple);
        assertEquals(bi(1309), attrBId);
        assertEquals(REFERENCE, attrBType);
        assertEquals("Attr B", attrBName);
        assertEquals(false, attrBMultiple);
        assertEquals(bi(1310), attrCId);
        assertEquals(DATE, attrCType);
        assertEquals("Attr C", attrCName);
        assertEquals(false, attrCMultiple);
    }

    @Test
    @InSequence(3)
    public void testUpdate_shouldUpdateRelation() throws Exception {
        typeHelper.createObjectType(1311, "Type");
        attrHelper.createAttribute(1312, TEXT, "Attr A", true, null);
        attrHelper.createAttribute(1313, REFERENCE, "Attr B", false, bi(1311));
        attrHelper.createAttribute(1314, DATE, "Attr C", null, null);
        attrHelper.createAttribute(1315, LIST, "Attr D", true, bi(1311));
        attrHelper.linkAttributesToObjectType(1311, 1312, 1313, 1314);

        transaction.begin();

        ObjectType type = typeProvider.getById(bi(1311));
        Attribute attrD = attrProvider.getById(bi(1315));
        List<Attribute> attributes = type.getAttributes();
        attributes.remove(0);
        attributes.add(attrD);

        transaction.commit();

        Map<String, Object> attrAData = attrHelper.readAttribute(bi(1312));
        Map<String, Object> attrBData = attrHelper.readAttribute(bi(1313));
        Map<String, Object> attrCData = attrHelper.readAttribute(bi(1314));
        Map<String, Object> attrDData = attrHelper.readAttribute(bi(1315));
        List<BigInteger> attrIds = attrHelper.readAttributesByObjectType(bi(1311));

        assertNull(attrAData.get("objectType"));
        assertNull(attrAData.get("order"));
        assertEquals(bi(1311), attrBData.get("objectType"));
        assertEquals(0, attrBData.get("order"));
        assertEquals(bi(1311), attrCData.get("objectType"));
        assertEquals(1, attrCData.get("order"));
        assertEquals(bi(1311), attrDData.get("objectType"));
        assertEquals(2, attrDData.get("order"));
        assertEquals(3, attrIds.size());
        assertEquals(bi(1313), attrIds.get(0));
        assertEquals(bi(1314), attrIds.get(1));
        assertEquals(bi(1315), attrIds.get(2));
    }

    @Test
    @InSequence(4)
    public void testUpdate_shouldRollbackUpdate() throws Exception {
        typeHelper.createObjectType(1316, "Type");
        attrHelper.createAttribute(1317, TEXT, "Attr A", true, null);
        attrHelper.createAttribute(1318, REFERENCE, "Attr B", false, bi(1316));
        attrHelper.createAttribute(1319, DATE, "Attr C", null, null);
        attrHelper.createAttribute(1320, LIST, "Attr D", true, bi(1316));
        attrHelper.linkAttributesToObjectType(1316, 1317, 1318, 1319);

        transaction.begin();

        ObjectType type = typeProvider.getById(bi(1316));
        Attribute attrD = attrProvider.getById(bi(1320));
        List<Attribute> attributes = type.getAttributes();
        attributes.remove(0);
        attributes.add(attrD);

        transaction.rollback();

        Map<String, Object> attrAData = attrHelper.readAttribute(bi(1317));
        Map<String, Object> attrBData = attrHelper.readAttribute(bi(1318));
        Map<String, Object> attrCData = attrHelper.readAttribute(bi(1319));
        Map<String, Object> attrDData = attrHelper.readAttribute(bi(1320));
        List<BigInteger> attrIds = attrHelper.readAttributesByObjectType(bi(1316));

        assertEquals(bi(1316), attrAData.get("objectType"));
        assertEquals(0, attrAData.get("order"));
        assertEquals(bi(1316), attrBData.get("objectType"));
        assertEquals(1, attrBData.get("order"));
        assertEquals(bi(1316), attrCData.get("objectType"));
        assertEquals(2, attrCData.get("order"));
        assertNull(attrDData.get("objectType"));
        assertNull(attrDData.get("order"));
        assertEquals(3, attrIds.size());
        assertEquals(bi(1317), attrIds.get(0));
        assertEquals(bi(1318), attrIds.get(1));
        assertEquals(bi(1319), attrIds.get(2));
    }

    @Test
    @InSequence(5)
    public void testRemove_shouldUnlink() throws Exception {
        typeHelper.createObjectType(1320, "Type");
        attrHelper.createAttribute(1321, TEXT, "Attr A", true, null);
        attrHelper.createAttribute(1322, REFERENCE, "Attr B", false, null);
        attrHelper.linkAttributesToObjectType(1320, 1321, 1322);

        transaction.begin();

        ObjectType type = typeProvider.getById(bi(1320));
        typeProvider.remove(type);

        transaction.commit();

        Map<String, Object> attrAData = attrHelper.readAttribute(bi(1321));
        Map<String, Object> attrBData = attrHelper.readAttribute(bi(1322));
        List<BigInteger> attrIds = attrHelper.readAttributesByObjectType(bi(1320));
        assertNull(attrAData.get("objectType"));
        assertNull(attrAData.get("order"));
        assertNull(attrBData.get("objectType"));
        assertNull(attrBData.get("order"));
        assertTrue(attrIds.isEmpty());
    }

    @Test
    @InSequence(6)
    public void testRemove_shouldRollbackUnlink() throws Exception {
        typeHelper.createObjectType(1323, "Type");
        attrHelper.createAttribute(1324, TEXT, "Attr A", true, null);
        attrHelper.createAttribute(1325, REFERENCE, "Attr B", false, null);
        attrHelper.linkAttributesToObjectType(1323, 1324, 1325);

        transaction.begin();

        ObjectType type = typeProvider.getById(bi(1323));
        typeProvider.remove(type);

        transaction.rollback();

        Map<String, Object> attrAData = attrHelper.readAttribute(bi(1324));
        Map<String, Object> attrBData = attrHelper.readAttribute(bi(1325));
        List<BigInteger> attrIds = attrHelper.readAttributesByObjectType(bi(1323));
        assertEquals(bi(1323), attrAData.get("objectType"));
        assertEquals(0, attrAData.get("order"));
        assertEquals(bi(1323), attrBData.get("objectType"));
        assertEquals(1, attrBData.get("order"));
        assertEquals(2, attrIds.size());
        assertEquals(bi(1324), attrIds.get(0));
        assertEquals(bi(1325), attrIds.get(1));
    }
}
