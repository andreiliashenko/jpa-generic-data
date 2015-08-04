package com.anli.generalization.data;

import com.anli.generalization.data.access.metadata.ObjectTypeProvider;
import com.anli.generalization.data.entities.metadata.ObjectType;
import com.anli.generalization.data.factory.JpaProviderFactory;
import com.anli.generalization.data.utils.ObjectTypeHelper;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class ObjectTypeTest {

    @Deployment
    public static Archive createDeployment() {
        return getDeployment();
    }

    @Resource(lookup = "java:/jdbc/integration_testing")
    private DataSource dataSource;

    @Resource
    private UserTransaction transaction;

    private ObjectTypeHelper helper;

    private ObjectTypeProvider provider;

    @Before
    public void setUp() {
        helper = new ObjectTypeHelper(dataSource);
        provider = JpaProviderFactory.getInstance().getObjectTypeProvider();
    }

    @Test
    @InSequence(0)
    public void testCreate_shouldCreateEmptyWithKey() throws Exception {
        transaction.begin();

        ObjectType type = provider.create(null);
        BigInteger id = type.getId();

        transaction.commit();

        assertNotNull(id);

        Map<String, Object> typeData = helper.readObjectType(id);

        assertEquals(id, typeData.get("id"));
        assertNull(typeData.get("name"));
        assertNull(typeData.get("parentType"));
    }

    @Test
    @InSequence(1)
    public void testCreate_shouldCreateWithData() throws Exception {
        helper.createObjectType(1101, "Parent name");
        helper.createObjectType(1102, "Child A name");
        helper.createObjectType(1103, "Child B name");

        transaction.begin();

        ObjectType parent = provider.getById(bi(1101));
        ObjectType childA = provider.getById(bi(1102));
        ObjectType childB = provider.getById(bi(1103));
        ObjectType type = provider.create(parent);
        BigInteger id = type.getId();
        type.setName("createdName");
        childA.setParent(type);
        childB.setParent(type);
        Collection<ObjectType> parentChildren = parent.getChildren();
        ObjectType parentChild = parentChildren.iterator().next();
        Collection<ObjectType> children = type.getChildren();
        ObjectType collectionChildA = null;
        ObjectType collectionChildB = null;
        for (ObjectType child : children) {
            if (bi(1102).equals(child.getId())) {
                collectionChildA = child;
            } else if (bi(1103).equals(child.getId())) {
                collectionChildB = child;
            } else {
                fail();
            }
        }
        if (collectionChildA == null) {
            fail();
            return;
        }
        if (collectionChildB == null) {
            fail();
            return;
        }
        assertEquals(1, parentChildren.size());
        assertSame(type, parentChild);
        assertSame(childA, collectionChildA);
        assertSame(childB, collectionChildB);

        transaction.commit();

        assertNotNull(id);

        Map<String, Object> typeData = helper.readObjectType(id);

        assertEquals(id, typeData.get("id"));
        assertEquals("createdName", typeData.get("name"));
        assertEquals(bi(1101), typeData.get("parentType"));

        Map<String, Object> childAData = helper.readObjectType(bi(1102));

        assertEquals(bi(1102), childAData.get("id"));
        assertEquals("Child A name", childAData.get("name"));
        assertEquals(id, childAData.get("parentType"));

        Map<String, Object> childBData = helper.readObjectType(bi(1103));

        assertEquals(bi(1103), childBData.get("id"));
        assertEquals("Child B name", childBData.get("name"));
        assertEquals(id, childBData.get("parentType"));

        Collection<BigInteger> parentChildrenIds = helper.readObjectTypesByParent(bi(1101));

        assertEquals(1, parentChildrenIds.size());
        assertEquals(id, parentChildrenIds.iterator().next());

        Collection<BigInteger> typeChildrenIds = helper.readObjectTypesByParent(id);

        assertEquals(2, typeChildrenIds.size());
        assertTrue(typeChildrenIds.contains(bi(1102)));
        assertTrue(typeChildrenIds.contains(bi(1103)));
    }

    @Test
    @InSequence(2)
    public void testCreate_shouldRollbackCreation() throws Exception {
        helper.createObjectType(1104, "Parent name");
        helper.createObjectType(1105, "Child A name");
        helper.createObjectType(1106, "Child B name");

        transaction.begin();

        ObjectType parent = provider.getById(bi(1104));
        ObjectType childA = provider.getById(bi(1105));
        ObjectType childB = provider.getById(bi(1106));
        ObjectType type = provider.create(parent);
        BigInteger id = type.getId();
        type.setName("createdName");
        childA.setParent(type);
        childB.setParent(type);

        transaction.rollback();

        assertNotNull(id);

        Map<String, Object> typeData = helper.readObjectType(id);

        assertNull(typeData);

        Map<String, Object> childAData = helper.readObjectType(bi(1105));

        assertEquals(bi(1105), childAData.get("id"));
        assertEquals("Child A name", childAData.get("name"));
        assertNull(childAData.get("parentType"));

        Map<String, Object> childBData = helper.readObjectType(bi(1106));

        assertEquals(bi(1106), childBData.get("id"));
        assertEquals("Child B name", childBData.get("name"));
        assertNull(childBData.get("parentType"));

        Collection<BigInteger> parentChildrenIds = helper.readObjectTypesByParent(bi(1104));

        assertTrue(parentChildrenIds.isEmpty());

        Collection<BigInteger> typeChildrenIds = helper.readObjectTypesByParent(id);

        assertTrue(typeChildrenIds.isEmpty());
    }

    @Test(expected = IllegalTransactionStateException.class)
    @InSequence(3)
    public void testCreation_shouldForbidNonTransactionalCall() {
        provider.create(null);
    }

    @Test
    @InSequence(4)
    public void testReading_shouldReadWithoutEdit() throws Exception {
        helper.createObjectType(1107, "Parent name");
        helper.createObjectType(1108, "Child A name");
        helper.createObjectType(1109, "Child B name");
        helper.createObjectType(1110, "Read name");
        helper.linkObjectTypesToParent(1107, 1110);
        helper.linkObjectTypesToParent(1110, 1108, 1109);
        transaction.begin();

        ObjectType type = provider.getById(bi(1110));
        BigInteger id = type.getId();
        String name = type.getName();
        ObjectType parent = type.getParent();
        BigInteger parentId = parent.getId();
        String parentName = parent.getName();
        ObjectType parentParent = parent.getParent();
        Collection<ObjectType> parentChildren = parent.getChildren();
        Collection<ObjectType> children = type.getChildren();

        assertNull(parentParent);
        assertEquals(1, parentChildren.size());
        assertSame(type, parentChildren.iterator().next());
        assertEquals(2, children.size());

        ObjectType childA = null;
        ObjectType childB = null;
        for (ObjectType child : children) {
            if (bi(1108).equals(child.getId())) {
                childA = child;
            } else if (bi(1109).equals(child.getId())) {
                childB = child;
            } else {
                fail();
            }
        }
        if (childA == null) {
            fail();
            return;
        }
        if (childB == null) {
            fail();
            return;
        }

        String childAName = childA.getName();
        ObjectType childAParent = childA.getParent();
        Collection<ObjectType> childAChildren = childA.getChildren();
        String childBName = childB.getName();
        ObjectType childBParent = childB.getParent();
        Collection<ObjectType> childBChildren = childB.getChildren();

        assertSame(type, childAParent);
        assertTrue(childAChildren.isEmpty());
        assertSame(type, childBParent);
        assertTrue(childBChildren.isEmpty());

        transaction.commit();

        assertEquals(bi(1110), id);
        assertEquals("Read name", name);
        assertEquals(bi(1107), parentId);
        assertEquals("Parent name", parentName);
        assertEquals("Child A name", childAName);
        assertEquals("Child B name", childBName);

        Map<String, Object> typeData = helper.readObjectType(bi(1110));

        assertEquals(id, typeData.get("id"));
        assertEquals("Read name", typeData.get("name"));
        assertEquals(bi(1107), typeData.get("parentType"));

        Map<String, Object> parentData = helper.readObjectType(bi(1107));

        assertEquals(bi(1107), parentData.get("id"));
        assertEquals("Parent name", parentData.get("name"));
        assertNull(parentData.get("parentType"));

        Map<String, Object> childAData = helper.readObjectType(bi(1108));

        assertEquals(bi(1108), childAData.get("id"));
        assertEquals("Child A name", childAData.get("name"));
        assertEquals(bi(1110), childAData.get("parentType"));

        Map<String, Object> childBData = helper.readObjectType(bi(1109));

        assertEquals(bi(1109), childBData.get("id"));
        assertEquals("Child B name", childBData.get("name"));
        assertEquals(bi(1110), childBData.get("parentType"));

        Collection<BigInteger> parentChildrenIds = helper.readObjectTypesByParent(bi(1107));

        assertEquals(1, parentChildrenIds.size());
        assertEquals(bi(1110), parentChildrenIds.iterator().next());

        Collection<BigInteger> typeChildrenIds = helper.readObjectTypesByParent(bi(1110));

        assertEquals(2, typeChildrenIds.size());
        assertTrue(typeChildrenIds.contains(bi(1108)));
        assertTrue(typeChildrenIds.contains(bi(1109)));
    }

    @Test
    @InSequence(5)
    public void testReading_shouldReadNull() throws Exception {
        transaction.begin();

        ObjectType type = provider.getById(bi(1111));

        transaction.commit();

        assertNull(type);
    }

    @Test(expected = IllegalTransactionStateException.class)
    @InSequence(6)
    public void testReading_shouldForbidNonTransactionalCall() {
        helper.createObjectType(1130, "Created Name");
        provider.getById(bi(1130));
    }

    @Test(expected = IllegalArgumentException.class)
    @InSequence(7)
    public void testReading_shouldCheckNullId() throws Exception {
        transaction.begin();

        provider.getById(null);

        transaction.commit();
    }

    @Test
    @InSequence(8)
    public void testUpdate_shouldUpdateData() throws Exception {
        helper.createObjectType(1112, "Initial parent name");
        helper.createObjectType(1113, "Initial child A name");
        helper.createObjectType(1114, "Initial child B name");
        helper.createObjectType(1115, "Initial name");
        helper.linkObjectTypesToParent(1112, 1115);
        helper.linkObjectTypesToParent(1115, 1113, 1114);

        transaction.begin();

        ObjectType type = provider.getById(bi(1115));
        ObjectType parent = type.getParent();
        Collection<ObjectType> parentChildren = parent.getChildren();
        Collection<ObjectType> children = type.getChildren();
        Iterator<ObjectType> childrenIterator = children.iterator();
        ObjectType childA = childrenIterator.next();
        ObjectType childB = childrenIterator.next();
        if (!bi(1113).equals(childA.getId())) {
            ObjectType temp = childB;
            childB = childA;
            childA = temp;
        }
        childB.setParent(parent);
        childA.setParent(null);
        type.setName("Updated name");
        parent.setName("Updated parent name");
        childA.setName("Updated child A name");
        childB.setName("Updated child B name");

        transaction.commit();

        Map<String, Object> typeData = helper.readObjectType(bi(1115));

        assertEquals(bi(1115), typeData.get("id"));
        assertEquals("Updated name", typeData.get("name"));
        assertEquals(bi(1112), typeData.get("parentType"));

        Map<String, Object> parentData = helper.readObjectType(bi(1112));

        assertEquals(bi(1112), parentData.get("id"));
        assertEquals("Updated parent name", parentData.get("name"));
        assertNull(parentData.get("parentType"));

        Map<String, Object> childAData = helper.readObjectType(bi(1113));

        assertEquals(bi(1113), childAData.get("id"));
        assertEquals("Updated child A name", childAData.get("name"));
        assertNull(childAData.get("parentType"));

        Map<String, Object> childBData = helper.readObjectType(bi(1114));

        assertEquals(bi(1114), childBData.get("id"));
        assertEquals("Updated child B name", childBData.get("name"));
        assertEquals(bi(1112), childBData.get("parentType"));

        Collection<BigInteger> parentChildrenIds = helper.readObjectTypesByParent(bi(1112));
        Collection<BigInteger> childrenIds = helper.readObjectTypesByParent(bi(1115));
        Collection<BigInteger> childAChildrenIds = helper.readObjectTypesByParent(bi(1113));
        Collection<BigInteger> childBChildrenIds = helper.readObjectTypesByParent(bi(1114));

        assertEquals(2, parentChildrenIds.size());
        assertTrue(parentChildrenIds.contains(bi(1114)));
        assertTrue(parentChildrenIds.contains(bi(1115)));
        assertTrue(childrenIds.isEmpty());
        assertTrue(childAChildrenIds.isEmpty());
        assertTrue(childBChildrenIds.isEmpty());
    }

    @Test
    @InSequence(9)
    public void testUpdate_shouldRollbackUpdate() throws Exception {
        helper.createObjectType(1116, "Initial parent name");
        helper.createObjectType(1117, "Initial child A name");
        helper.createObjectType(1118, "Initial child B name");
        helper.createObjectType(1119, "Initial name");
        helper.linkObjectTypesToParent(1116, 1119);
        helper.linkObjectTypesToParent(1119, 1117, 1118);

        transaction.begin();

        ObjectType type = provider.getById(bi(1119));
        ObjectType parent = type.getParent();
        Collection<ObjectType> parentChildren = parent.getChildren();
        Collection<ObjectType> children = type.getChildren();
        Iterator<ObjectType> childrenIterator = children.iterator();
        ObjectType childA = childrenIterator.next();
        ObjectType childB = childrenIterator.next();
        if (!bi(1117).equals(childA.getId())) {
            ObjectType temp = childB;
            childB = childA;
            childA = temp;
        }
        childB.setParent(parent);
        childA.setParent(null);
        type.setName("Updated name");
        parent.setName("Updated parent name");
        childA.setName("Updated child A name");

        transaction.rollback();

        Map<String, Object> typeData = helper.readObjectType(bi(1119));

        assertEquals(bi(1119), typeData.get("id"));
        assertEquals("Initial name", typeData.get("name"));
        assertEquals(bi(1116), typeData.get("parentType"));

        Map<String, Object> parentData = helper.readObjectType(bi(1116));

        assertEquals(bi(1116), parentData.get("id"));
        assertEquals("Initial parent name", parentData.get("name"));
        assertNull(parentData.get("parentType"));

        Map<String, Object> childAData = helper.readObjectType(bi(1117));

        assertEquals(bi(1117), childAData.get("id"));
        assertEquals("Initial child A name", childAData.get("name"));
        assertEquals(bi(1119), childAData.get("parentType"));

        Map<String, Object> childBData = helper.readObjectType(bi(1118));

        assertEquals(bi(1118), childBData.get("id"));
        assertEquals("Initial child B name", childBData.get("name"));
        assertEquals(bi(1119), childBData.get("parentType"));

        Collection<BigInteger> parentChildrenIds = helper.readObjectTypesByParent(bi(1116));
        Collection<BigInteger> childrenIds = helper.readObjectTypesByParent(bi(1119));
        Collection<BigInteger> childAChildrenIds = helper.readObjectTypesByParent(bi(1117));
        Collection<BigInteger> childBChildrenIds = helper.readObjectTypesByParent(bi(1118));

        assertEquals(1, parentChildrenIds.size());
        assertEquals(bi(1119), parentChildrenIds.iterator().next());
        assertEquals(2, childrenIds.size());
        assertTrue(childrenIds.contains(bi(1117)));
        assertTrue(childrenIds.contains(bi(1118)));
        assertTrue(childAChildrenIds.isEmpty());
        assertTrue(childBChildrenIds.isEmpty());
    }

    @Test
    @InSequence(10)
    public void testRemove_shouldRemoveData() throws Exception {
        helper.createObjectType(1120, "Parent name");
        helper.createObjectType(1121, "Child A name");
        helper.createObjectType(1122, "Child B name");
        helper.createObjectType(1123, "Removed name");
        helper.linkObjectTypesToParent(1120, 1123);
        helper.linkObjectTypesToParent(1123, 1121, 1122);

        transaction.begin();

        ObjectType type = provider.getById(bi(1123));
        provider.remove(type);

        transaction.commit();

        Map<String, Object> typeData = helper.readObjectType(bi(1123));

        assertNull(typeData);

        Map<String, Object> parentData = helper.readObjectType(bi(1120));

        assertEquals(bi(1120), parentData.get("id"));
        assertEquals("Parent name", parentData.get("name"));
        assertNull(parentData.get("parentType"));

        Map<String, Object> childAData = helper.readObjectType(bi(1121));

        assertEquals(bi(1121), childAData.get("id"));
        assertEquals("Child A name", childAData.get("name"));
        assertNull(childAData.get("parentType"));

        Map<String, Object> childBData = helper.readObjectType(bi(1122));

        assertEquals(bi(1122), childBData.get("id"));
        assertEquals("Child B name", childBData.get("name"));
        assertNull(childBData.get("parentType"));

        Collection<BigInteger> parentChildrenIds = helper.readObjectTypesByParent(bi(1120));
        Collection<BigInteger> childrenIds = helper.readObjectTypesByParent(bi(1123));
        Collection<BigInteger> childAChildrenIds = helper.readObjectTypesByParent(bi(1121));
        Collection<BigInteger> childBChildrenIds = helper.readObjectTypesByParent(bi(1122));

        assertTrue(parentChildrenIds.isEmpty());
        assertTrue(childrenIds.isEmpty());
        assertTrue(childAChildrenIds.isEmpty());
        assertTrue(childBChildrenIds.isEmpty());
    }

    @Test
    @InSequence(12)
    public void testRemove_shouldRollbackRemove() throws Exception {
        helper.createObjectType(1125, "Parent name");
        helper.createObjectType(1126, "Child A name");
        helper.createObjectType(1127, "Child B name");
        helper.createObjectType(1128, "Rollback removed name");
        helper.linkObjectTypesToParent(1125, 1128);
        helper.linkObjectTypesToParent(1128, 1126, 1127);

        transaction.begin();

        ObjectType type = provider.getById(bi(1128));
        provider.remove(type);

        transaction.rollback();

        Map<String, Object> typeData = helper.readObjectType(bi(1128));

        assertEquals(bi(1128), typeData.get("id"));
        assertEquals("Rollback removed name", typeData.get("name"));
        assertEquals(bi(1125), typeData.get("parentType"));

        Map<String, Object> parentData = helper.readObjectType(bi(1125));

        assertEquals(bi(1125), parentData.get("id"));
        assertEquals("Parent name", parentData.get("name"));
        assertNull(parentData.get("parentType"));

        Map<String, Object> childAData = helper.readObjectType(bi(1126));

        assertEquals(bi(1126), childAData.get("id"));
        assertEquals("Child A name", childAData.get("name"));
        assertEquals(bi(1128), childAData.get("parentType"));

        Map<String, Object> childBData = helper.readObjectType(bi(1127));

        assertEquals(bi(1127), childBData.get("id"));
        assertEquals("Child B name", childBData.get("name"));
        assertEquals(bi(1128), childBData.get("parentType"));

        Collection<BigInteger> parentChildrenIds = helper.readObjectTypesByParent(bi(1125));
        Collection<BigInteger> childrenIds = helper.readObjectTypesByParent(bi(1128));
        Collection<BigInteger> childAChildrenIds = helper.readObjectTypesByParent(bi(1126));
        Collection<BigInteger> childBChildrenIds = helper.readObjectTypesByParent(bi(1127));

        assertEquals(1, parentChildrenIds.size());
        assertEquals(bi(1128), parentChildrenIds.iterator().next());
        assertEquals(2, childrenIds.size());
        assertTrue(childrenIds.contains(bi(1126)));
        assertTrue(childrenIds.contains(bi(1127)));
        assertTrue(childAChildrenIds.isEmpty());
        assertTrue(childBChildrenIds.isEmpty());
    }

    @Test(expected = IllegalTransactionStateException.class)
    @InSequence(13)
    public void testRemove_shouldForbidNonTransactionalCall() throws Exception {
        helper.createObjectType(1129, "Non-removed name");

        transaction.begin();

        ObjectType type = provider.getById(bi(1129));

        transaction.commit();

        provider.remove(type);
    }

    @Test(expected = IllegalArgumentException.class)
    @InSequence(14)
    public void testRemove_shouldCheckNull() throws Exception {
        transaction.begin();

        provider.remove(null);

        transaction.commit();
    }
}
