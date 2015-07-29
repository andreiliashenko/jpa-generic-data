package com.anli.generalization.data;

import com.anli.generalization.data.access.metadata.ListEntryProvider;
import com.anli.generalization.data.entities.metadata.ListEntry;
import com.anli.generalization.data.factory.JpaProviderFactory;
import com.anli.generalization.data.utils.ListEntryHelper;
import java.math.BigInteger;
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(Arquillian.class)
public class ListEntryTest {

    @Deployment
    public static Archive createDeployment() {
        return getDeployment();
    }

    @Resource(lookup = "java:/jdbc/integration_testing")
    private DataSource dataSource;

    @Resource
    private UserTransaction transaction;

    private ListEntryHelper helper;

    private ListEntryProvider provider;

    @Before
    public void setUp() {
        helper = new ListEntryHelper(dataSource);
        provider = JpaProviderFactory.getInstance().getListEntryManager();
    }

    @Test
    @InSequence(0)
    public void testCreate_shouldCreateEmptyWithKey() throws Exception {
        transaction.begin();

        ListEntry listEntry = provider.create();
        BigInteger id = listEntry.getId();

        transaction.commit();

        assertNotNull(id);

        Map<String, Object> entryData = helper.readListEntry(id);

        assertEquals(id, entryData.get("id"));
        assertNull(entryData.get("entryValue"));
        assertNull(entryData.get("attribute"));
        assertNull(entryData.get("order"));
    }

    @Test
    @InSequence(1)
    public void testCreate_shouldCreateWithData() throws Exception {
        transaction.begin();

        ListEntry listEntry = provider.create();
        BigInteger id = listEntry.getId();
        listEntry.setEntryValue("created value");

        transaction.commit();

        assertNotNull(id);

        Map<String, Object> entryData = helper.readListEntry(id);

        assertEquals(id, entryData.get("id"));
        assertEquals("created value", entryData.get("entryValue"));
        assertNull(entryData.get("attribute"));
        assertNull(entryData.get("order"));
    }

    @Test
    @InSequence(2)
    public void testCreate_shouldRollbackCreation() throws Exception {
        transaction.begin();

        ListEntry entry = provider.create();
        BigInteger id = entry.getId();
        entry.setEntryValue("rollback value");

        transaction.rollback();

        Map<String, Object> entryData = helper.readListEntry(id);

        assertNull(entryData);
    }

    @Test(expected = IllegalTransactionStateException.class)
    @InSequence(3)
    public void testCreation_shouldForbidNonTransactionalCall() {
        provider.create();
    }

    @Test
    @InSequence(4)
    public void testReading_shouldReadWithoutEdit() throws Exception {
        helper.createListEntry(1001, "readed value");

        transaction.begin();

        ListEntry entry = provider.getById(BigInteger.valueOf(1001));
        BigInteger id = entry.getId();
        String value = entry.getEntryValue();

        transaction.commit();

        assertEquals(BigInteger.valueOf(1001), id);
        assertEquals("readed value", value);

        Map<String, Object> entryData = helper.readListEntry(BigInteger.valueOf(1001));

        assertEquals(BigInteger.valueOf(1001), entryData.get("id"));
        assertEquals("readed value", entryData.get("entryValue"));
        assertNull(entryData.get("attribute"));
        assertNull(entryData.get("order"));
    }

    @Test
    @InSequence(5)
    public void testReading_shouldReadNull() throws Exception {
        transaction.begin();

        ListEntry entry = provider.getById(BigInteger.valueOf(1002));

        transaction.commit();

        assertNull(entry);
    }

    @Test(expected = IllegalTransactionStateException.class)
    @InSequence(6)
    public void testReading_shouldForbidNonTransactionalCall() {
        helper.createListEntry(1003, "created value");
        provider.getById(BigInteger.valueOf(1003));
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
        helper.createListEntry(1004, "initial value");

        transaction.begin();

        ListEntry entry = provider.getById(BigInteger.valueOf(1004));
        entry.setEntryValue("updated value");

        transaction.commit();

        Map<String, Object> entryData = helper.readListEntry(BigInteger.valueOf(1004));

        assertEquals(BigInteger.valueOf(1004), entryData.get("id"));
        assertEquals("updated value", entryData.get("entryValue"));
        assertNull(entryData.get("attribute"));
        assertNull(entryData.get("order"));
    }

    @Test
    @InSequence(9)
    public void testUpdate_shouldRollbackUpdate() throws Exception {
        helper.createListEntry(1005, "initial value");

        transaction.begin();

        ListEntry entry = provider.getById(BigInteger.valueOf(1005));
        entry.setEntryValue("updated value");

        transaction.rollback();

        Map<String, Object> entryData = helper.readListEntry(BigInteger.valueOf(1005));

        assertEquals(BigInteger.valueOf(1005), entryData.get("id"));
        assertEquals("initial value", entryData.get("entryValue"));
        assertNull(entryData.get("attribute"));
        assertNull(entryData.get("order"));
    }

    @Test
    @InSequence(10)
    public void testRemove_shouldRemoveData() throws Exception {
        helper.createListEntry(1006, "removed value");

        transaction.begin();

        ListEntry entry = provider.getById(BigInteger.valueOf(1006));
        entry.setEntryValue("updatedValue");
        provider.remove(entry);

        transaction.commit();

        Map<String, Object> data = helper.readListEntry(BigInteger.valueOf(1006));

        assertNull(data);
    }

    @Test
    @InSequence(12)
    public void testRemove_shouldRollbackRemove() throws Exception {
        helper.createListEntry(1007, "rollback remove value");

        transaction.begin();

        ListEntry entry = provider.getById(BigInteger.valueOf(1007));
        provider.remove(entry);

        transaction.rollback();

        Map<String, Object> entryData = helper.readListEntry(BigInteger.valueOf(1007));

        assertEquals(BigInteger.valueOf(1007), entryData.get("id"));
        assertEquals("rollback remove value", entryData.get("entryValue"));
        assertNull(entryData.get("attribute"));
        assertNull(entryData.get("order"));
    }

    @Test(expected = IllegalTransactionStateException.class)
    @InSequence(13)
    public void testRemove_shouldForbidNonTransactionalCall() throws Exception {
        helper.createListEntry(1008, "non-removed value");

        transaction.begin();

        ListEntry entry = provider.getById(BigInteger.valueOf(1008));

        transaction.commit();

        provider.remove(entry);
    }

    @Test(expected = IllegalArgumentException.class)
    @InSequence(14)
    public void testRemove_shouldCheckNull() throws Exception {
        transaction.begin();

        provider.remove(null);

        transaction.commit();
    }
}
