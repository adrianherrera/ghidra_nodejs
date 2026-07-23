package v8_bytecode.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import ghidra.program.model.listing.Program;
import ghidra.program.model.util.PropertyMapManager;
import ghidra.util.Msg;
import ghidra.util.ObjectStorage;
import ghidra.util.PrivateSaveable;
import ghidra.util.exception.DuplicateNameException;

public final class InstructionsStorage extends PrivateSaveable {
	private long addr;
	private ScopeInfoStore store;

	public InstructionsStorage() {

	}

	private InstructionsStorage(long addr, ScopeInfoStore store) {
		this.addr = addr;
		this.store = store;
	}

	public ScopeInfoStore getScopeInfo() {
		return store;
	}

	public static void create(Program program, long address, ScopeInfoStore store) {
		PropertyMapManager mgr = program.getUsrPropertyManager();

		InstructionsStorage result = new InstructionsStorage(address, store);

		try {
			var map = mgr.createObjectPropertyMap(String.format("IS_%d", address), InstructionsStorage.class);
			map.add(program.getAddressFactory().getDefaultAddressSpace().getAddress(address), result);
		} catch (DuplicateNameException e) {
		}
	}

	public static InstructionsStorage load(Program program, long address) {
		PropertyMapManager mgr = program.getUsrPropertyManager();
		var map = mgr.getObjectPropertyMap(String.format("IS_%d", address));

		if (map == null) {
			return null;
		}

		return (InstructionsStorage) map.get(program.getAddressFactory().getDefaultAddressSpace().getAddress(address));
	}

	@Override
	public Class<?>[] getObjectStorageFields() {
		return new Class[] {byte[].class};
	}

	@Override
	public void save(ObjectStorage objStorage) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		try (DataOutputStream out = new DataOutputStream(bos)) {
			out.writeLong(addr);
			ScopeInfoStore.writeNullable(out, store);

			out.flush();
			objStorage.putBytes(bos.toByteArray());
		} catch (IOException e) {
			Msg.error(this, "Failed to save InstructionsStorage", e);
		}
	}

	@Override
	public void restore(ObjectStorage objStorage) {
		ByteArrayInputStream bis = new ByteArrayInputStream(objStorage.getBytes());

		try (DataInputStream in = new DataInputStream(bis)) {
			addr = in.readLong();
			store = ScopeInfoStore.readNullable(in);
		} catch (IOException e) {
			Msg.error(this, "Failed to restore InstructionsStorage", e);
		}
	}

	@Override
	public int getSchemaVersion() {
		return 0;
	}

	@Override
	public boolean isUpgradeable(int oldSchemaVersion) {
		return false;
	}

	@Override
	public boolean upgrade(ObjectStorage oldObjStorage, int oldSchemaVersion, ObjectStorage currentObjStorage) {
		return false;
	}
}
