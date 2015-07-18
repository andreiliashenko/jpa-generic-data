package com.anli.generalization.data.entities.proxy;

import com.anli.generalization.data.entities.DataObject;
import com.anli.generalization.data.entities.jpa.ChildrenGroup;
import com.anli.generalization.data.entities.jpa.JpaDataObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import javax.inject.Inject;
import javax.inject.Named;
import org.springframework.context.annotation.Scope;

@Named
@Scope("prototype")
public class ChildrenProxyCollection implements Collection<DataObject> {

    protected static final String UNMODIFIABLE_ERROR =
            "Data Object child collection is unmodifiable";

    protected final DataObjectProxyBuilder proxyBuilder;

    protected LinkedList<ChildrenGroup> childrenGroups = new LinkedList<>();

    @Inject
    public ChildrenProxyCollection(DataObjectProxyBuilder proxyBuilder) {
        this.proxyBuilder = proxyBuilder;
    }

    @Override
    public int size() {
        int size = 0;
        for (ChildrenGroup group : childrenGroups) {
            size += group.getChildren().size();
        }
        return size;
    }

    @Override
    public boolean isEmpty() {
        for (ChildrenGroup group : childrenGroups) {
            if (!group.getChildren().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean contains(Object o) {
        for (ChildrenGroup group : childrenGroups) {
            if (group.getChildren().contains(o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterator<DataObject> iterator() {
        return new ProxyIterator();
    }

    @Override
    public Object[] toArray() {
        return fetchAll().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return fetchAll().toArray(a);
    }

    @Override
    public boolean add(DataObject e) {
        throw new UnsupportedOperationException(UNMODIFIABLE_ERROR);
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException(UNMODIFIABLE_ERROR);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return fetchAll().containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends DataObject> c) {
        throw new UnsupportedOperationException(UNMODIFIABLE_ERROR);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException(UNMODIFIABLE_ERROR);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException(UNMODIFIABLE_ERROR);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException(UNMODIFIABLE_ERROR);
    }

    protected Collection<DataObject> fetchAll() {
        ArrayList<DataObject> all = new ArrayList<>(size());
        for (ChildrenGroup group : childrenGroups) {
            for (JpaDataObject object : group.getChildren()) {
                all.add(proxyBuilder.getProxy(object));
            }
        }
        return all;
    }

    public void addChildrenGroup(ChildrenGroup group) {
        childrenGroups.add(group);
    }

    protected class ProxyIterator implements Iterator<DataObject> {

        protected Iterator<ChildrenGroup> groupIterator;
        protected Iterator<JpaDataObject> objectIterator;

        public ProxyIterator() {
            groupIterator = childrenGroups.iterator();
        }

        @Override
        public boolean hasNext() {
            while (objectIterator == null || !objectIterator.hasNext()) {
                if (groupIterator.hasNext()) {
                    ChildrenGroup newGroup = groupIterator.next();
                    objectIterator = newGroup.getChildren().iterator();
                } else {
                    return false;
                }
            }
            return true;
        }

        @Override
        public DataObject next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return proxyBuilder.getProxy(objectIterator.next());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException(UNMODIFIABLE_ERROR);
        }
    }
}
