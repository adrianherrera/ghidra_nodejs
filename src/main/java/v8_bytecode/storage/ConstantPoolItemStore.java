package v8_bytecode.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import v8_bytecode.RootObject;

public final class ConstantPoolItemStore {

	private static final int TAG_NULL = 0;
	private static final int TAG_STRING = 1;
	private static final int TAG_INTEGER = 2;
	private static final int TAG_LONG = 3;
	private static final int TAG_DOUBLE = 4;
	private static final int TAG_ROOT_OBJECT = 5;
	private static final int TAG_SHARED_FUNCTION = 6;
	private static final int TAG_SCOPE_INFO = 7;
	private static final int TAG_ARRAY = 8;
	private static final int TAG_TUPLE = 9;

	private final Object item;
	private final long address;

	public ConstantPoolItemStore(final Object item, long address) {
		this.item = item;
		this.address = address;
	}

	public Object getItem() {
		return item;
	}

	public long getAddress() {
		return address;
	}

	void writeTo(DataOutputStream out) throws IOException {
		out.writeLong(address);
		if (item == null) {
			out.writeByte(TAG_NULL);
		} else if (item instanceof String s) {
			out.writeByte(TAG_STRING);
			out.writeUTF(s);
		} else if (item instanceof Integer i) {
			out.writeByte(TAG_INTEGER);
			out.writeInt(i);
		} else if (item instanceof Long l) {
			out.writeByte(TAG_LONG);
			out.writeLong(l);
		} else if (item instanceof Double d) {
			out.writeByte(TAG_DOUBLE);
			out.writeDouble(d);
		} else if (item instanceof RootObject r) {
			out.writeByte(TAG_ROOT_OBJECT);
			out.writeUTF(r.getName());
			out.writeUTF(r.getType());
		} else if (item instanceof SharedFunctionStore sf) {
			out.writeByte(TAG_SHARED_FUNCTION);
			sf.writeTo(out);
		} else if (item instanceof ScopeInfoStore si) {
			out.writeByte(TAG_SCOPE_INFO);
			si.writeTo(out);
		} else if (item instanceof ArrayStore a) {
			out.writeByte(TAG_ARRAY);
			a.writeTo(out);
		} else if (item instanceof TupleStore t) {
			out.writeByte(TAG_TUPLE);
			t.writeTo(out);
		} else {
			out.writeByte(TAG_NULL);
		}
	}

	static ConstantPoolItemStore readFrom(DataInputStream in) throws IOException {
		long address = in.readLong();
		int tag = in.readByte();
		Object item;
		switch (tag) {
			case TAG_STRING:
				item = in.readUTF();
				break;
			case TAG_INTEGER:
				item = in.readInt();
				break;
			case TAG_LONG:
				item = in.readLong();
				break;
			case TAG_DOUBLE:
				item = in.readDouble();
				break;
			case TAG_ROOT_OBJECT:
				item = new RootObject(in.readUTF(), in.readUTF());
				break;
			case TAG_SHARED_FUNCTION:
				item = SharedFunctionStore.readFrom(in);
				break;
			case TAG_SCOPE_INFO:
				item = ScopeInfoStore.readFrom(in);
				break;
			case TAG_ARRAY:
				item = ArrayStore.readFrom(in);
				break;
			case TAG_TUPLE:
				item = TupleStore.readFrom(in);
				break;
			default:
				item = null;
				break;
		}
		return new ConstantPoolItemStore(item, address);
	}
}
