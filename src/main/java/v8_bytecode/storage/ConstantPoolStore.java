package v8_bytecode.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class ConstantPoolStore {

	private final List<ConstantPoolItemStore> items;

	public ConstantPoolStore(final List<ConstantPoolItemStore> items) {
		this.items = items;
	}

	public Object getConstItem(int index) {
		return items.get(index).getItem();
	}

	public long getConstItemAddress(int index) {
		return items.get(index).getAddress();
	}

	void writeTo(DataOutputStream out) throws IOException {
		out.writeInt(items.size());
		for (final ConstantPoolItemStore item : items) {
			item.writeTo(out);
		}
	}

	static ConstantPoolStore readFrom(DataInputStream in) throws IOException {
		int count = in.readInt();
		List<ConstantPoolItemStore> items = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			items.add(ConstantPoolItemStore.readFrom(in));
		}
		return new ConstantPoolStore(items);
	}
}
