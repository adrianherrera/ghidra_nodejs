package v8_bytecode.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import v8_bytecode.structs.ArrayStruct;

public final class ArrayStore {
	private final String name;

	private ArrayStore(final String name) {
		this.name = name;
	}

	public static ArrayStore fromStruct(final ArrayStruct struct) {
		return new ArrayStore(struct.getName());
	}

	public String getName() {
		return name;
	}

	void writeTo(DataOutputStream out) throws IOException {
		out.writeUTF(name);
	}

	static ArrayStore readFrom(DataInputStream in) throws IOException {
		return new ArrayStore(in.readUTF());
	}
}
