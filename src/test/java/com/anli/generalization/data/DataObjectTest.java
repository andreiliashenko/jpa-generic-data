package com.anli.generalization.data;

import com.anli.generalization.data.access.DataObjectProvider;
import com.anli.generalization.data.access.metadata.ObjectTypeProvider;
import com.anli.generalization.data.entities.DataObject;
import com.anli.generalization.data.entities.metadata.ObjectType;
import com.anli.generalization.data.factory.JpaProviderFactory;
import com.anli.generalization.data.utils.DataObjectHelper;
import com.anli.generalization.data.utils.ObjectTypeHelper;
import java.math.BigInteger;
import java.util.Collection;
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

import static com.anli.generalization.data.utils.CommonDeployment.getDeployment;
import static com.anli.generalization.data.utils.ValueFactory.bi;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class DataObjectTest {

    @Deployment
    public static Archive createDeployment() {
        return getDeployment();
    }

    @Resource(lookup = "java:/jdbc/integration_testing")
    private DataSource dataSource;

    @Resource
    private UserTransaction transaction;

    private ObjectTypeHelper typeHelper;
    private DataObjectHelper objectHelper;

    private ObjectTypeProvider typeProvider;
    private DataObjectProvider objectProvider;

    @Before
    public void setUp() {
        typeHelper = new ObjectTypeHelper(dataSource);
        objectHelper = new DataObjectHelper(dataSource);
        typeProvider = JpaProviderFactory.getInstance().getObjectTypeProvider();
        objectProvider = JpaProviderFactory.getInstance().getDataObjectProvider();
        Map<String, Object> typeA = typeHelper.readObjectType(bi(1401));
        if (typeA == null) {
            typeHelper.createObjectType(1401, "Type A");
        }
        Map<String, Object> typeB = typeHelper.readObjectType(bi(1402));
        if (typeB == null) {
            typeHelper.createObjectType(1402, "Type B");
        }
        Map<String, Object> typeC = typeHelper.readObjectType(bi(1403));
        if (typeC == null) {
            typeHelper.createObjectType(1403, "Type C");
        }
        typeHelper.linkObjectTypesToParent(1402, 1403);
    }

    @Test
    @InSequence(0)
    public void testCreate_shouldCreateEmptyWithType() throws Exception {
        transaction.begin();

        ObjectType typeA = typeProvider.getById(bi(1401));

        DataObject dataObject = objectProvider.create(typeA);
        BigInteger id = dataObject.getId();

        transaction.commit();

        assertNotNull(id);

        Map<String, Object> objectData = objectHelper.readObject(id);

        assertEquals(id, objectData.get("id"));
        assertNull(objectData.get("name"));
        assertNull(objectData.get("description"));
        assertEquals(bi(1401), objectData.get("objectType"));
        assertNull(objectData.get("group"));
    }

    @Test
    @InSequence(1)
    public void testCreate_shouldCreateHierarchyWithData() throws Exception {
        transaction.begin();

        ObjectType typeA = typeProvider.getById(bi(1401));
        ObjectType typeB = typeProvider.getById(bi(1402));
        ObjectType typeC = typeProvider.getById(bi(1403));

        DataObject root = objectProvider.create(typeA);
        BigInteger rootId = root.getId();
        root.setName("Root");
        root.setDescription("Root Description");
        DataObject childA = objectProvider.create(typeA);
        BigInteger childAId = childA.getId();
        childA.setName("Child A");
        childA.setDescription("Child A Description");
        root.addChild(childA);
        DataObject childBA = objectProvider.create(typeB);
        BigInteger childBAId = childBA.getId();
        childBA.setName("Child BA");
        childBA.setDescription("Child BA Description");
        DataObject childBB = objectProvider.create(typeB);
        BigInteger childBBId = childBB.getId();
        childBB.setName("Child BB");
        childBB.setDescription("Child BB Description");
        root.addChild(childBA);
        root.addChild(childBB);
        DataObject childC = objectProvider.create(typeC);
        BigInteger childCId = childC.getId();
        childC.setName("Child C");
        childC.setDescription("Child C Description");
        root.addChild(childC);

        assertNull(root.getParent());
        assertEquals(root, childA.getParent());
        assertEquals(root, childBA.getParent());
        assertEquals(root, childBB.getParent());
        assertEquals(root, childC.getParent());

        transaction.commit();

        assertNotNull(rootId);
        assertNotNull(childAId);
        assertNotNull(childBAId);
        assertNotNull(childBBId);
        assertNotNull(childCId);

        Map<String, Object> rootData = objectHelper.readObject(rootId);
        Map<String, Object> childAData = objectHelper.readObject(childAId);
        Map<String, Object> childBAData = objectHelper.readObject(childBAId);
        Map<String, Object> childBBData = objectHelper.readObject(childBBId);
        Map<String, Object> childCData = objectHelper.readObject(childCId);

        assertEquals(rootId, rootData.get("id"));
        assertEquals("Root", rootData.get("name"));
        assertEquals("Root Description", rootData.get("description"));
        assertEquals(bi(1401), rootData.get("objectType"));
        assertNull(rootData.get("group"));

        assertEquals(childAId, childAData.get("id"));
        assertEquals("Child A", childAData.get("name"));
        assertEquals("Child A Description", childAData.get("description"));
        assertEquals(bi(1401), childAData.get("objectType"));
        BigInteger childAGroupId = (BigInteger) childAData.get("group");
        assertNotNull(childAGroupId);

        assertEquals(childBAId, childBAData.get("id"));
        assertEquals("Child BA", childBAData.get("name"));
        assertEquals("Child BA Description", childBAData.get("description"));
        assertEquals(bi(1402), childBAData.get("objectType"));
        BigInteger childBAGroupId = (BigInteger) childBAData.get("group");
        assertNotNull(childBAGroupId);

        assertEquals(childBBId, childBBData.get("id"));
        assertEquals("Child BB", childBBData.get("name"));
        assertEquals("Child BB Description", childBBData.get("description"));
        assertEquals(bi(1402), childBBData.get("objectType"));
        BigInteger childBBGroupId = (BigInteger) childBBData.get("group");
        assertNotNull(childBBGroupId);

        assertEquals(childCId, childCData.get("id"));
        assertEquals("Child C", childCData.get("name"));
        assertEquals("Child C Description", childCData.get("description"));
        assertEquals(bi(1403), childCData.get("objectType"));
        BigInteger childCGroupId = (BigInteger) childCData.get("group");
        assertNotNull(childCGroupId);

        assertEquals(childBAGroupId, childBBGroupId);
        assertFalse(childBAGroupId.equals(childAGroupId));
        assertFalse(childCGroupId.equals(childAGroupId));
        assertFalse(childBAGroupId.equals(childCGroupId));

        Map<String, Object> groupA = objectHelper.readChildrenGroup(childAGroupId);
        Map<String, Object> groupB = objectHelper.readChildrenGroup(childBAGroupId);
        Map<String, Object> groupC = objectHelper.readChildrenGroup(childCGroupId);
        Collection<BigInteger> rootChildrenGroups = objectHelper.readGroupsByParent(rootId);

        assertEquals(childAGroupId, groupA.get("id"));
        assertEquals(rootId, groupA.get("parent"));
        assertEquals(bi(1401), groupA.get("objectType"));

        assertEquals(childBAGroupId, groupB.get("id"));
        assertEquals(rootId, groupB.get("parent"));
        assertEquals(bi(1402), groupB.get("objectType"));

        assertEquals(childCGroupId, groupC.get("id"));
        assertEquals(rootId, groupC.get("parent"));
        assertEquals(bi(1403), groupC.get("objectType"));

        assertEquals(3, rootChildrenGroups.size());
        assertTrue(rootChildrenGroups.contains(childAGroupId));
        assertTrue(rootChildrenGroups.contains(childBAGroupId));
        assertTrue(rootChildrenGroups.contains(childCGroupId));
    }

    @Test
    @InSequence(2)
    public void testCreate_shouldRollbackCreation() throws Exception {
        transaction.begin();

        ObjectType typeA = typeProvider.getById(bi(1401));
        ObjectType typeB = typeProvider.getById(bi(1402));
        ObjectType typeC = typeProvider.getById(bi(1403));

        DataObject root = objectProvider.create(typeA);
        BigInteger rootId = root.getId();
        root.setName("Root");
        root.setDescription("Root Description");
        DataObject childA = objectProvider.create(typeA);
        BigInteger childAId = childA.getId();
        childA.setName("Child A");
        childA.setDescription("Child A Description");
        root.addChild(childA);
        DataObject childBA = objectProvider.create(typeB);
        BigInteger childBAId = childBA.getId();
        childBA.setName("Child BA");
        childBA.setDescription("Child BA Description");
        DataObject childBB = objectProvider.create(typeB);
        BigInteger childBBId = childBB.getId();
        childBB.setName("Child BB");
        childBB.setDescription("Child BB Description");
        root.addChild(childBA);
        root.addChild(childBB);
        DataObject childC = objectProvider.create(typeC);
        BigInteger childCId = childC.getId();
        childC.setName("Child C");
        childC.setDescription("Child C Description");
        root.addChild(childC);

        assertNull(root.getParent());
        assertEquals(root, childA.getParent());
        assertEquals(root, childBA.getParent());
        assertEquals(root, childBB.getParent());
        assertEquals(root, childC.getParent());

        transaction.rollback();

        assertNotNull(rootId);
        assertNotNull(childAId);
        assertNotNull(childBAId);
        assertNotNull(childBBId);
        assertNotNull(childCId);

        Map<String, Object> rootData = objectHelper.readObject(rootId);
        Map<String, Object> childAData = objectHelper.readObject(childAId);
        Map<String, Object> childBAData = objectHelper.readObject(childBAId);
        Map<String, Object> childBBData = objectHelper.readObject(childBBId);
        Map<String, Object> childCData = objectHelper.readObject(childCId);

        assertNull(rootData);
        assertNull(childAData);
        assertNull(childBAData);
        assertNull(childBBData);
        assertNull(childCData);
    }

    @Test(expected = IllegalTransactionStateException.class)
    @InSequence(3)
    public void testCreation_shouldForbidNonTransactionalCall() throws Exception {
        transaction.begin();

        ObjectType typeA = typeProvider.getById(bi(1401));

        transaction.commit();

        objectProvider.create(typeA);
    }

    @Test(expected = IllegalArgumentException.class)
    @InSequence(4)
    public void testCreation_shouldNotCreateWithNullType() throws Exception {
        transaction.begin();

        objectProvider.create(null);

        transaction.commit();
    }

    @Test
    @InSequence(5)
    public void testReading_shouldReadHierarchy() throws Exception {
        objectHelper.createObject(1404, "Read Root", "Read Root Description",
                bi(1401), null);
        objectHelper.createChildrenGroup(1405, bi(1404), bi(1402));
        objectHelper.createObject(1406, "Read Child BA", "Read Child BA Description",
                bi(1402), bi(1405));
        objectHelper.createObject(1407, "Read Child BB", "Read Child BB Description",
                bi(1402), bi(1405));
        objectHelper.createChildrenGroup(1408, bi(1404), bi(1403));
        objectHelper.createObject(1409, "Read Child C", "Read Child C Description",
                bi(1403), bi(1408));

        transaction.begin();

        ObjectType typeA = typeProvider.getById(bi(1401));
        ObjectType typeB = typeProvider.getById(bi(1402));
        ObjectType typeC = typeProvider.getById(bi(1403));

        DataObject root = objectProvider.getById(bi(1404));
        BigInteger rootId = root.getId();
        String rootName = root.getName();
        String rootDescription = root.getDescription();
        ObjectType rootType = root.getObjectType();
        DataObject rootParent = root.getParent();
        Collection<DataObject> typeAChildren = root.getChildren(typeA, true);
        Collection<DataObject> typeBChildren = root.getChildren(typeB, false);
        Collection<DataObject> typeCChildren = root.getChildren(typeC, false);
        Collection<DataObject> typeBCChildren = root.getChildren(typeB, true);

        assertSame(typeA, rootType);
        assertTrue(typeAChildren.isEmpty());
        assertEquals(2, typeBChildren.size());
        assertEquals(1, typeCChildren.size());
        assertEquals(3, typeBCChildren.size());

        DataObject childBA = null;
        DataObject childBB = null;
        DataObject childC = null;

        for (DataObject dataObject : root.getChildren(typeB, true)) {
            if (bi(1406).equals(dataObject.getId())) {
                childBA = dataObject;
            } else if (bi(1407).equals(dataObject.getId())) {
                childBB = dataObject;
            } else if (bi(1409).equals(dataObject.getId())) {
                childC = dataObject;
            } else {
                fail();
            }
        }
        if (childBA == null) {
            fail();
            return;
        }
        if (childBB == null) {
            fail();
            return;
        }
        if (childC == null) {
            fail();
            return;
        }
        assertTrue(typeBChildren.containsAll(asList(childBB, childBA)));
        assertTrue(typeCChildren.contains(childC));
        assertTrue(typeBCChildren.contains(childBB));
        assertTrue(typeBCChildren.contains(childBA));
        assertTrue(typeBCChildren.contains(childC));

        BigInteger childBAId = childBA.getId();
        String childBAName = childBA.getName();
        String childBADescription = childBA.getDescription();
        ObjectType childBAType = childBA.getObjectType();
        DataObject childBAParent = childBA.getParent();

        BigInteger childBBId = childBB.getId();
        String childBBName = childBB.getName();
        String childBBDescription = childBB.getDescription();
        ObjectType childBBType = childBB.getObjectType();
        DataObject childBBParent = childBB.getParent();

        BigInteger childCId = childC.getId();
        String childCName = childC.getName();
        String childCDescription = childC.getDescription();
        ObjectType childCType = childC.getObjectType();
        DataObject childCParent = childC.getParent();

        assertSame(childBAType, typeB);
        assertSame(childBBType, typeB);
        assertSame(childCType, typeC);

        assertNull(rootParent);
        assertEquals(root, childBAParent);
        assertEquals(root, childBBParent);
        assertEquals(root, childCParent);

        transaction.commit();

        assertEquals(bi(1404), rootId);
        assertEquals("Read Root", rootName);
        assertEquals("Read Root Description", rootDescription);

        assertEquals(bi(1406), childBAId);
        assertEquals("Read Child BA", childBAName);
        assertEquals("Read Child BA Description", childBADescription);

        assertEquals(bi(1407), childBBId);
        assertEquals("Read Child BB", childBBName);
        assertEquals("Read Child BB Description", childBBDescription);

        assertEquals(bi(1409), childCId);
        assertEquals("Read Child C", childCName);
        assertEquals("Read Child C Description", childCDescription);
    }

    @Test
    @InSequence(6)
    public void testReading_shouldReadNull() throws Exception {
        transaction.begin();

        DataObject dataObject = objectProvider.getById(bi(1410));

        transaction.commit();

        assertNull(dataObject);
    }

    @Test(expected = IllegalTransactionStateException.class)
    @InSequence(7)
    public void testReading_shouldForbidNonTransactionalCall() {
        objectHelper.createObject(1411, "Created", "Created Description", bi(1401), null);
        objectProvider.getById(bi(1411));
    }

    @Test(expected = IllegalArgumentException.class)
    @InSequence(8)
    public void testReading_shouldCheckNullId() throws Exception {
        transaction.begin();

        objectProvider.getById(null);

        transaction.commit();
    }

    @Test(expected = IllegalArgumentException.class)
    @InSequence(9)
    public void testReading_shouldCheckNullObjectType() throws Exception {
        objectHelper.createObject(1412, "Created", "Created Description", bi(1401), null);

        transaction.begin();

        DataObject dataObject = objectProvider.getById(bi(1412));
        dataObject.getChildren(null, true);

        transaction.commit();
    }

    @Test
    @InSequence(10)
    public void testUpdate_shouldUpdateMainData() throws Exception {
        objectHelper.createObject(1413, "Initial Root", "Initial Root Description",
                bi(1401), null);
        objectHelper.createChildrenGroup(1414, bi(1413), bi(1402));
        objectHelper.createObject(1415, "Initial Child", "Initial Child Description",
                bi(1402), bi(1414));

        transaction.begin();

        ObjectType typeB = typeProvider.getById(bi(1402));
        DataObject root = objectProvider.getById(bi(1413));
        DataObject child = root.getChildren(typeB, false).iterator().next();
        root.setName("Updated Root");
        root.setDescription("Updated Root Description");
        child.setName("Updated Child");
        child.setDescription("Updated Child Description");

        transaction.commit();

        Map<String, Object> rootData = objectHelper.readObject(bi(1413));
        Map<String, Object> childData = objectHelper.readObject(bi(1415));
        Map<String, Object> childrenGroup = objectHelper.readChildrenGroup(bi(1414));
        Collection<BigInteger> childrenGroups = objectHelper.readGroupsByParent(bi(1413));

        assertEquals(bi(1413), rootData.get("id"));
        assertEquals("Updated Root", rootData.get("name"));
        assertEquals("Updated Root Description", rootData.get("description"));
        assertEquals(bi(1401), rootData.get("objectType"));
        assertNull(rootData.get("group"));

        assertEquals(bi(1415), childData.get("id"));
        assertEquals("Updated Child", childData.get("name"));
        assertEquals("Updated Child Description", childData.get("description"));
        assertEquals(bi(1402), childData.get("objectType"));
        assertEquals(bi(1414), childData.get("group"));

        assertEquals(bi(1414), childrenGroup.get("id"));
        assertEquals(bi(1413), childrenGroup.get("parent"));
        assertEquals(bi(1402), childrenGroup.get("objectType"));

        assertEquals(1, childrenGroups.size());
        assertEquals(bi(1414), childrenGroups.iterator().next());
    }

    @Test
    @InSequence(11)
    public void testUpdate_shouldUpdateHierarchy() throws Exception {
        objectHelper.createObject(1416, "Root", null, bi(1401), null);
        objectHelper.createChildrenGroup(1417, bi(1416), bi(1401));
        objectHelper.createObject(1418, "A", null, bi(1401), bi(1417));
        objectHelper.createChildrenGroup(1419, bi(1416), bi(1402));
        objectHelper.createObject(1420, "BA", null, bi(1402), bi(1419));
        objectHelper.createObject(1421, "BB", null, bi(1402), bi(1419));
        objectHelper.createObject(1422, "BC", null, bi(1402), null);

        transaction.begin();

        ObjectType typeA = typeProvider.getById(bi(1401));
        DataObject root = objectProvider.getById(bi(1416));
        DataObject bc = objectProvider.getById(bi(1422));
        DataObject bb = objectProvider.getById(bi(1421));
        DataObject a = root.getChildren(typeA, true).iterator().next();
        root.removeChild(a);
        a.addChild(root);
        root.removeChild(bb);
        root.addChild(bc);

        transaction.commit();

        BigInteger rootParentGroup = (BigInteger) objectHelper.readObject(bi(1416)).get("group");
        BigInteger aParentGroup = (BigInteger) objectHelper.readObject(bi(1418)).get("group");
        BigInteger baParentGroup = (BigInteger) objectHelper.readObject(bi(1420)).get("group");
        BigInteger bbParentGroup = (BigInteger) objectHelper.readObject(bi(1421)).get("group");
        BigInteger bcParentGroup = (BigInteger) objectHelper.readObject(bi(1422)).get("group");

        assertNotNull(rootParentGroup);
        assertNull(aParentGroup);
        assertEquals(bi(1419), baParentGroup);
        assertNull(bbParentGroup);
        assertEquals(bi(1419), bcParentGroup);

        Map<String, Object> removedGroupData = objectHelper.readChildrenGroup(bi(1417));
        Map<String, Object> createdGroupData = objectHelper.readChildrenGroup(rootParentGroup);
        Map<String, Object> updatedGroupData = objectHelper.readChildrenGroup(bi(1419));
        Collection<BigInteger> rootChildrenGroups = objectHelper.readGroupsByParent(bi(1416));
        Collection<BigInteger> aChildrenGroups = objectHelper.readGroupsByParent(bi(1418));

        assertNull(removedGroupData);

        assertEquals(rootParentGroup, createdGroupData.get("id"));
        assertEquals(bi(1418), createdGroupData.get("parent"));
        assertEquals(bi(1401), createdGroupData.get("objectType"));

        assertEquals(bi(1419), updatedGroupData.get("id"));
        assertEquals(bi(1416), updatedGroupData.get("parent"));
        assertEquals(bi(1402), updatedGroupData.get("objectType"));

        assertEquals(1, rootChildrenGroups.size());
        assertEquals(bi(1419), rootChildrenGroups.iterator().next());
        assertEquals(1, aChildrenGroups.size());
        assertEquals(rootParentGroup, aChildrenGroups.iterator().next());
    }

    @Test
    @InSequence(12)
    public void testUpdate_shouldRollbackUpdate() throws Exception {
        objectHelper.createObject(1423, "Initial Root", "Initial Root Description",
                bi(1401), null);
        objectHelper.createChildrenGroup(1424, bi(1423), bi(1402));
        objectHelper.createObject(1425, "Initial B", "Initial B Description",
                bi(1402), bi(1424));

        transaction.begin();

        DataObject root = objectProvider.getById(bi(1423));
        DataObject b = objectProvider.getById(bi(1425));
        root.setName("Rollback Root");
        root.setDescription("Rollback Root Description");
        b.setName("Rollback B");
        b.setName("Rollback B Description");
        root.removeChild(b);

        transaction.rollback();

        Map<String, Object> rootData = objectHelper.readObject(bi(1423));
        Map<String, Object> bData = objectHelper.readObject(bi(1425));
        Map<String, Object> groupData = objectHelper.readChildrenGroup(bi(1424));

        assertEquals(bi(1423), rootData.get("id"));
        assertEquals("Initial Root", rootData.get("name"));
        assertEquals("Initial Root Description", rootData.get("description"));

        assertEquals(bi(1425), bData.get("id"));
        assertEquals("Initial B", bData.get("name"));
        assertEquals("Initial B Description", bData.get("description"));
        assertEquals(bi(1424), bData.get("group"));

        assertEquals(bi(1424), groupData.get("id"));
        assertEquals(bi(1402), groupData.get("objectType"));
        assertEquals(bi(1423), groupData.get("parent"));
    }

    @Test(expected = IllegalArgumentException.class)
    @InSequence(13)
    public void testUpdate_shouldNotAddNullChild() throws Exception {
        objectHelper.createObject(1426, "Null Child", null, bi(1401), null);

        transaction.begin();

        DataObject nullChild = objectProvider.getById(bi(1426));
        nullChild.addChild(null);

        transaction.commit();
    }

    @Test(expected = IllegalArgumentException.class)
    @InSequence(14)
    public void testUpdate_shouldNotRemoveNullChild() throws Exception {
        objectHelper.createObject(1427, "Remove Null Child", null, bi(1401), null);

        transaction.begin();

        DataObject removeNullChild = objectProvider.getById(bi(1427));
        removeNullChild.removeChild(null);

        transaction.commit();
    }

    @Test
    @InSequence(15)
    public void testRemove_shouldRemoveData() throws Exception {
        objectHelper.createObject(1428, "Root", null, bi(1401), null);
        objectHelper.createChildrenGroup(1429, bi(1428), bi(1401));
        objectHelper.createChildrenGroup(1430, bi(1428), bi(1402));
        objectHelper.createObject(1431, "A", null, bi(1401), bi(1429));
        objectHelper.createObject(1432, "B", null, bi(1402), bi(1430));

        transaction.begin();

        DataObject root = objectProvider.getById(bi(1428));
        objectProvider.remove(root);

        transaction.commit();

        Map<String, Object> rootData = objectHelper.readObject(bi(1428));
        BigInteger aGroup = (BigInteger) objectHelper.readObject(bi(1431)).get("group");
        BigInteger bGroup = (BigInteger) objectHelper.readObject(bi(1432)).get("group");
        Map<String, Object> aGroupData = objectHelper.readChildrenGroup(bi(1429));
        Map<String, Object> bGroupData = objectHelper.readChildrenGroup(bi(1430));
        Collection<BigInteger> rootGroups = objectHelper.readGroupsByParent(bi(1428));

        assertNull(rootData);
        assertNull(aGroup);
        assertNull(bGroup);
        assertNull(aGroupData);
        assertNull(bGroupData);
        assertTrue(rootGroups.isEmpty());
    }

    @Test
    @InSequence(16)
    public void testRemove_shouldRollbackRemove() throws Exception {
        objectHelper.createObject(1433, "Root", null, bi(1401), null);
        objectHelper.createChildrenGroup(1434, bi(1433), bi(1401));
        objectHelper.createChildrenGroup(1435, bi(1433), bi(1402));
        objectHelper.createObject(1436, "A", null, bi(1401), bi(1434));
        objectHelper.createObject(1437, "B", null, bi(1402), bi(1435));

        transaction.begin();

        DataObject root = objectProvider.getById(bi(1433));
        objectProvider.remove(root);

        transaction.rollback();

        Map<String, Object> rootData = objectHelper.readObject(bi(1433));
        BigInteger aGroup = (BigInteger) objectHelper.readObject(bi(1436)).get("group");
        BigInteger bGroup = (BigInteger) objectHelper.readObject(bi(1437)).get("group");
        Map<String, Object> aGroupData = objectHelper.readChildrenGroup(bi(1434));
        Map<String, Object> bGroupData = objectHelper.readChildrenGroup(bi(1435));
        Collection<BigInteger> rootGroups = objectHelper.readGroupsByParent(bi(1433));

        assertNotNull(rootData);
        assertEquals(bi(1434), aGroup);
        assertEquals(bi(1435), bGroup);
        assertEquals(bi(1433), aGroupData.get("parent"));
        assertEquals(bi(1433), bGroupData.get("parent"));
        assertEquals(2, rootGroups.size());
        assertTrue(rootGroups.contains(bi(1434)));
        assertTrue(rootGroups.contains(bi(1435)));
    }

    @Test(expected = IllegalTransactionStateException.class)
    @InSequence(17)
    public void testRemove_shouldForbidNonTransactionalCall() throws Exception {
        objectHelper.createObject(1438, "Not Removed", null, bi(1403), null);

        transaction.begin();

        DataObject toRemove = objectProvider.getById(bi(1438));

        transaction.commit();

        objectProvider.remove(toRemove);
    }

    @Test(expected = IllegalArgumentException.class)
    @InSequence(18)
    public void testRemove_shouldCheckNull() throws Exception {
        transaction.begin();

        objectProvider.remove(null);

        transaction.commit();
    }
}
