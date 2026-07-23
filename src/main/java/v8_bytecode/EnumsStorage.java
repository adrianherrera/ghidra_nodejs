package v8_bytecode;

import v8_bytecode.storage.RootsStore;
import v8_bytecode.storage.RuntimesIntrinsicsStore;

public final class EnumsStorage {

	private final RootsStore rootsEnum;
	private final RuntimesIntrinsicsStore runsIntrsStore;
	
	public EnumsStorage(final RootsStore rootsEnum,
			final RuntimesIntrinsicsStore runsIntrsStore) {
		this.rootsEnum = rootsEnum;
		this.runsIntrsStore = runsIntrsStore;
	}

	public RootsStore getRoots() {
		return rootsEnum;
	}
	
	public RuntimesIntrinsicsStore getRuntimesIntrinsicsStore() {
		return runsIntrsStore;
	}
}
