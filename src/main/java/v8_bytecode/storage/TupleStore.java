package v8_bytecode.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import v8_bytecode.structs.TupleStruct;

public final class TupleStore {
	private final String name;

	private TupleStore(final String name) {
		this.name = name;
	}

	public static TupleStore fromStruct(final TupleStruct struct) {
		return new TupleStore(struct.getName());
	}

	public String getName() {
		return name;
	}

	void writeTo(DataOutputStream out) throws IOException {
		out.writeUTF(name);
	}

	static TupleStore readFrom(DataInputStream in) throws IOException {
		return new TupleStore(in.readUTF());
	}
}
