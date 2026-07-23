package v8_bytecode.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import v8_bytecode.RootObject;
import v8_bytecode.structs.ContextVarStruct;
import v8_bytecode.structs.ScopeInfoStruct;

public final class ScopeInfoStore {
	private final String name;
	private final long offset;
	private final List<ContextVarStore> ctxVars;
	private final ScopeInfoStore outerScope;

	private ScopeInfoStore(final String name, long offset, final List<ContextVarStore> ctxVars, final ScopeInfoStore outerScope) {
		this.name = name;
		this.offset = offset;
		this.ctxVars = ctxVars;
		this.outerScope = outerScope;
	}

	public static ScopeInfoStore fromStruct(final Object struct) {
		if (struct == null || struct instanceof RootObject) {
			return null;
		}

		final List<ContextVarStore> ctxVars1 = new ArrayList<>();

		for (final ContextVarStruct var : ((ScopeInfoStruct) struct).getContextVars()) {
			final ContextVarStore ctxVar;

			if (var == null) {
				ctxVar = null;
			} else {
				ctxVar = new ContextVarStore(var.getAddress().getOffset(), var.getName());
			}

			ctxVars1.add(ctxVar);
		}

		final ScopeInfoStore outerScope = ScopeInfoStore.fromStruct(((ScopeInfoStruct)struct).getOuterScope());
		return new ScopeInfoStore(((ScopeInfoStruct) struct).getName(), ((ScopeInfoStruct) struct).getAddress().getOffset(), ctxVars1, outerScope);
	}

	public String getName() {
		return name;
	}

	public long getOffset() {
		return offset;
	}

	public ContextVarStore getContextVar(int index) {
		if (index < ctxVars.size()) {
			return ctxVars.get(index);
		}

		if (outerScope == null) {
			return null;
		}

		return outerScope.getContextVar(index);
	}

	public ContextVarStore getContextVar(int index, int depth) {
		if (depth < 0) {
			depth = 0;
		}

		if (depth == 0 && index < ctxVars.size()) {
			return ctxVars.get(index);
		}

		return outerScope.getContextVar(index, depth - 1);
	}

	void writeTo(DataOutputStream out) throws IOException {
		out.writeUTF(name);
		out.writeLong(offset);
		out.writeInt(ctxVars.size());
		for (final ContextVarStore var : ctxVars) {
			out.writeBoolean(var != null);
			if (var != null) {
				var.writeTo(out);
			}
		}
		out.writeBoolean(outerScope != null);
		if (outerScope != null) {
			outerScope.writeTo(out);
		}
	}

	static ScopeInfoStore readFrom(DataInputStream in) throws IOException {
		String name = in.readUTF();
		long offset = in.readLong();
		int varCount = in.readInt();
		List<ContextVarStore> ctxVars = new ArrayList<>(varCount);
		for (int i = 0; i < varCount; i++) {
			if (in.readBoolean()) {
				ctxVars.add(ContextVarStore.readFrom(in));
			} else {
				ctxVars.add(null);
			}
		}
		ScopeInfoStore outerScope = null;
		if (in.readBoolean()) {
			outerScope = ScopeInfoStore.readFrom(in);
		}
		return new ScopeInfoStore(name, offset, ctxVars, outerScope);
	}

	static void writeNullable(DataOutputStream out, ScopeInfoStore scope) throws IOException {
		out.writeBoolean(scope != null);
		if (scope != null) {
			scope.writeTo(out);
		}
	}

	static ScopeInfoStore readNullable(DataInputStream in) throws IOException {
		if (in.readBoolean()) {
			return readFrom(in);
		}
		return null;
	}
}
