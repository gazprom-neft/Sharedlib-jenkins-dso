package com.gpn.pipeline

class ExtendDescription {

    private Script script

    ExtendDescription(Script script) {
        this.script = script
    }

    void addString(String key, String value) {
        script.currentBuild.description = script.currentBuild.description + "!!!$key = $value!!!" + "\n"
    }
}