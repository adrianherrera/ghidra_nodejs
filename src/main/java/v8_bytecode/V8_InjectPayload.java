package v8_bytecode;

import ghidra.app.plugin.processors.sleigh.PcodeEmit;
import ghidra.app.plugin.processors.sleigh.SleighLanguage;
import ghidra.program.model.lang.InjectContext;
import ghidra.program.model.lang.InjectPayload;
import ghidra.program.model.lang.InjectPayloadCallother;

public abstract class V8_InjectPayload extends InjectPayloadCallother {
	protected SleighLanguage language;
	protected long uniqueBase;
	private String sourceName;
	private String opName;

	public V8_InjectPayload(String sourceName, SleighLanguage language, long uniqBase, String opName) {
		super(sourceName);
		this.language = language;
		this.sourceName = sourceName;
		this.uniqueBase = uniqBase;
		this.opName = opName;

	}

	@Override
	public String getName() {
		return opName;
	}

	@Override
	public int getType() {
		return InjectPayload.CALLOTHERFIXUP_TYPE;
	}

	@Override
	public String getSource() {
		return sourceName;
	}

	@Override
	public int getParamShift() {
		return 0;
	}

	@Override
	public void inject(InjectContext context, PcodeEmit emit) {
		// Not used
	}

	@Override
	public boolean isFallThru() {
		return true;
	}

	@Override
	public InjectParameter[] getInput() {
		return null;
	}

	@Override
	public InjectParameter[] getOutput() {
		return null;
	}

}
