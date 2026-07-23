package v8_bytecode.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Program;
import ghidra.program.model.util.PropertyMapManager;
import ghidra.util.Msg;
import ghidra.util.ObjectStorage;
import ghidra.util.PrivateSaveable;
import ghidra.util.exception.DuplicateNameException;
import v8_bytecode.EnumsStorage;
import v8_bytecode.RootObject;
import v8_bytecode.RuntimeFuncArg;

public final class FuncsStorage extends PrivateSaveable {

	private static final long STOR_ADDR = 0x80000000L;

	private RootsStore rootsEnum = null;
	private RuntimesIntrinsicsStore runsIntrsStore = null;
	private Set<SharedFunctionStore> sharedFuncs = new HashSet<>();

	public FuncsStorage() {

	}

	private FuncsStorage(final EnumsStorage store) {
		this.rootsEnum = store.getRoots();
		this.runsIntrsStore = store.getRuntimesIntrinsicsStore();
	}

	public static FuncsStorage create(Program program, final EnumsStorage store) {
		PropertyMapManager mgr = program.getUsrPropertyManager();

		FuncsStorage result = new FuncsStorage(store);

		try {
			var map = mgr.createObjectPropertyMap("FS", FuncsStorage.class);
			map.add(program.getAddressFactory().getDefaultAddressSpace().getAddress(STOR_ADDR), result);
		} catch (DuplicateNameException e) {
		}

		return result;
	}

	public static FuncsStorage load(Program program) {
		PropertyMapManager mgr = program.getUsrPropertyManager();
		var map = mgr.getObjectPropertyMap("FS");

		return (FuncsStorage) map.get(program.getAddressFactory().getDefaultAddressSpace().getAddress(STOR_ADDR));
	}

	public void store(Program program) {
		int transId = program.startTransaction("Save FuncsStorage");

		PropertyMapManager mgr = program.getUsrPropertyManager();
		var map = mgr.getObjectPropertyMap("FS");
		map.remove(program.getAddressFactory().getDefaultAddressSpace().getAddress(STOR_ADDR));
		map.add(program.getAddressFactory().getDefaultAddressSpace().getAddress(STOR_ADDR), this);

		program.endTransaction(transId, true);
	}

	@Override
	public Class<?>[] getObjectStorageFields() {
		return new Class[] {byte[].class};
	}

	@Override
	public void save(ObjectStorage objStorage) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		try (DataOutputStream out = new DataOutputStream(bos)) {
			out.writeInt(sharedFuncs.size());
			for (final SharedFunctionStore sf : sharedFuncs) {
				sf.writeTo(out);
			}

			out.writeBoolean(rootsEnum != null);
			if (rootsEnum != null) {
				List<RootObject> roots = rootsEnum.getRoots();
				out.writeInt(roots.size());
				for (final RootObject root : roots) {
					out.writeUTF(root.getName());
					out.writeUTF(root.getType());
				}
			}

			out.writeBoolean(runsIntrsStore != null);
			if (runsIntrsStore != null) {
				List<String> names = runsIntrsStore.getNames();
				out.writeInt(names.size());
				for (final String name : names) {
					out.writeUTF(name);
				}
				int namesCount = runsIntrsStore.getNamesCount();
				out.writeInt(namesCount);
				for (int i = 0; i < namesCount; i++) {
					List<RuntimeFuncArg> args = runsIntrsStore.getArgs(i);
					out.writeInt(args.size());
					for (final RuntimeFuncArg arg : args) {
						out.writeUTF(arg.getName());
						String type = arg.getType();
						out.writeUTF(type != null ? type : "");
					}
				}
			}

			out.flush();
			objStorage.putBytes(bos.toByteArray());
		} catch (IOException e) {
			Msg.error(this, "Failed to save FuncsStorage", e);
		}
	}

	@Override
	public void restore(ObjectStorage objStorage) {
		ByteArrayInputStream bis = new ByteArrayInputStream(objStorage.getBytes());

		try (DataInputStream in = new DataInputStream(bis)) {
			int sfCount = in.readInt();
			sharedFuncs = new HashSet<>(sfCount);
			for (int i = 0; i < sfCount; i++) {
				sharedFuncs.add(SharedFunctionStore.readFrom(in));
			}

			if (in.readBoolean()) {
				int rootCount = in.readInt();
				List<RootObject> roots = new ArrayList<>(rootCount);
				for (int i = 0; i < rootCount; i++) {
					roots.add(new RootObject(in.readUTF(), in.readUTF()));
				}
				rootsEnum = new RootsStore(roots);
			}

			if (in.readBoolean()) {
				int nameCount = in.readInt();
				List<String> names = new ArrayList<>(nameCount);
				for (int i = 0; i < nameCount; i++) {
					names.add(in.readUTF());
				}
				int argsGroupCount = in.readInt();
				List<List<RuntimeFuncArg>> allArgs = new ArrayList<>(argsGroupCount);
				for (int i = 0; i < argsGroupCount; i++) {
					int argCount = in.readInt();
					List<RuntimeFuncArg> args = new ArrayList<>(argCount);
					for (int j = 0; j < argCount; j++) {
						args.add(new RuntimeFuncArg(in.readUTF(), in.readUTF()));
					}
					allArgs.add(args);
				}
				runsIntrsStore = new RuntimesIntrinsicsStore(names, allArgs);
			}
		} catch (IOException e) {
			Msg.error(this, "Failed to restore FuncsStorage", e);
		}
	}

	public RootsStore getRoots() {
		return rootsEnum;
	}

	public RuntimesIntrinsicsStore getRuntimesIntrinsicsStore() {
		return runsIntrsStore;
	}

	public void addToSharedFunctions(final SharedFunctionStore func) {
		sharedFuncs.add(func);
	}

	private SharedFunctionStore getSharedFunction(final Address addrInFunc) {
		for (final SharedFunctionStore func : sharedFuncs) {
			if (func.contains(addrInFunc.getOffset())) {
				return func;
			}
		}

		return null;
	}

	public ScopeInfoStore getScopeInfo(final Address addr, final String reg) {
		final SharedFunctionStore sharedFunc = getSharedFunction(addr);
		if (sharedFunc == null) {
			return null;
		}
		return sharedFunc.getScopeInfo(reg);
	}

	public ScopeInfoStore getOuterScopeInfo(final Address addr) {
		final SharedFunctionStore sharedFunc = getSharedFunction(addr);
		if (sharedFunc == null) {
			return null;
		}
		return sharedFunc.getOuterScopeInfo();
	}

	public void pushScopeInfo(final Address addr, final String reg, final ScopeInfoStore scope) {
		final SharedFunctionStore sharedFunc = getSharedFunction(addr);
		if (sharedFunc == null) {
			return;
		}
		sharedFunc.pushScopeInfo(reg, scope);
	}

	public ScopeInfoStore popScopeInfo(final Address addr, final String reg) {
		final SharedFunctionStore sharedFunc = getSharedFunction(addr);
		if (sharedFunc == null) {
			return null;
		}
		return sharedFunc.popScopeInfo(reg);
	}

	public Object getConstItem(final Address addr, int index) {
		final SharedFunctionStore sharedFunc = getSharedFunction(addr);
		if (sharedFunc == null) {
			return null;
		}
		final ConstantPoolStore cp = sharedFunc.getConstantPool();
		if (cp == null) {
			return null;
		}
		return cp.getConstItem(index);
	}

	public long getConstItemAddress(final Address addr, int index) {
		final SharedFunctionStore sharedFunc = getSharedFunction(addr);
		if (sharedFunc == null) {
			return -1;
		}
		final ConstantPoolStore cp = sharedFunc.getConstantPool();
		if (cp == null) {
			return -1;
		}
		return cp.getConstItemAddress(index);
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
