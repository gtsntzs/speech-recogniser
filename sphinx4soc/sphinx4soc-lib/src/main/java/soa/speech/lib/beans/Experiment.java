package soa.speech.recogniser.lib.beans;

import java.util.Arrays;

public class Experiment {

    private String name;// = "WSJ_clean_13dCep_16k_40mel_130Hz_6800Hz_temp";
    private String version;
    // database
    private String speechDatabase;
    // model data
    private String modelsName;// = "WSJ_clean_13dCep_16k_40mel_130Hz_6800Hz";
    private String dictDataName;
    private String dictFillerName;
    private String mdefFileName;
    private String[] modelDataNames;
    // configuration
    private String frontEndConfiguration;
    private String decoderConfiguration;
    // TODO use a rule based predicate defined by the database
    private int corpus;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public String getSpeechDatabase() {
        return speechDatabase;
    }
    public void setSpeechDatabase(String speechDatabase) {
        this.speechDatabase = speechDatabase;
    }
    public String getModelsName() {
        return modelsName;
    }
    public void setModelsName(String modelsName) {
        this.modelsName = modelsName;
    }
    public String getDictDataName() {
        return dictDataName;
    }
    public void setDictDataName(String dictDataName) {
        this.dictDataName = dictDataName;
    }
    public String getDictFillerName() {
        return dictFillerName;
    }
    public void setDictFillerName(String dictFillerName) {
        this.dictFillerName = dictFillerName;
    }
    public String getMdefFileName() {
        return mdefFileName;
    }
    public void setMdefFileName(String mdefFileName) {
        this.mdefFileName = mdefFileName;
    }
    public String[] getModelDataNames() {
        return modelDataNames;
    }
    public void setModelDataNames(String[] modelDataNames) {
        this.modelDataNames = modelDataNames;
    }
    public String getFrontEndConfiguration() {
        return frontEndConfiguration;
    }
    public void setFrontEndConfiguration(String frontEndConfiguration) {
        this.frontEndConfiguration = frontEndConfiguration;
    }
    public String getDecoderConfiguration() {
        return decoderConfiguration;
    }
    public void setDecoderConfiguration(String decoderConfiguration) {
        this.decoderConfiguration = decoderConfiguration;
    }
    public int getCorpus() {
        return corpus;
    }
    public void setCorpus(int corpus) {
        this.corpus = corpus;
    }
    @Override
    public String toString() {
        return "{\"experimentName\":\"" + name + "\",\"version\":\"" + version + "\",\"speechDatabase\":\"" + speechDatabase
                + "\",\"modelsName\":\"" + modelsName + "\",\"dictDataName\":\"" + dictDataName + "\",\"dictFillerName\":\"" + dictFillerName
                + "\",\"mdefFileName\":\"" + mdefFileName + "\",\"modelDataNames\":\"" + Arrays.toString(modelDataNames)
                + "\",\"frontEndConfiguration\":\"" + frontEndConfiguration + "\",\"decoderConfiguration\":\"" + decoderConfiguration
                + "\",\"corpus\":\"" + corpus + "\"}";
    }
    
}
