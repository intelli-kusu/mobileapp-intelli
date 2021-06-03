package com.intellicare.model.patientinfo;

import com.google.gson.annotations.SerializedName;
import com.intellicare.model.createvisitmodel.Allergy;
import com.intellicare.model.patientinfonew.Member;
import com.intellicare.model.patientinfonew.Relation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Patient {
    @SerializedName("fields")
    private List<Field> fields = null;
    @SerializedName("empty_fields")
    private Integer emptyFields;
    @SerializedName("dependents")
    private List<Object> dependents = null;
    @SerializedName("relations")
    private List<Relation> relations = null;
    @SerializedName("members")
    private List<Member> members = null;

    public List<Allergy> getPreviouslySelectedAllergies() {
        return previouslySelectedAllergies;
    }

    public void setPreviouslySelectedAllergies(List<Allergy> previouslySelectedAllergies) {
        this.previouslySelectedAllergies = previouslySelectedAllergies;
    }

    @SerializedName("allergies")
    private List<Allergy> previouslySelectedAllergies;

    public Map<String, Member> getMembersMap() {
        Map<String, Member> membersMap = new HashMap<>();
        if (members != null) {
            for (Member m : members) {
                membersMap.put(m.getRelation(), m);
            }
        }
        return membersMap;
    }

    public Map<String, String> getRelationsMap() {
        Map<String, String> relationsMap = new HashMap<>();
        if (relations != null) {
            for (Relation r : relations) {
                relationsMap.put(r.getRelation(), r.getPatientId());
            }
        }
        return relationsMap;
    }

    public Map<String, Field> getFieldMap() {
        Map<String, Field> fieldMap = new HashMap<>();
        if(fields != null) {
            for(Field f : fields) {
                fieldMap.put(f.getLabel(), f);
            }
        }
        return fieldMap;
    }

    public List<Relation> getRelations() {
        return relations;
    }

    public void setRelations(List<Relation> relations) {
        this.relations = relations;
    }

    public List<Member> getMembers() {
        return members;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public Integer getEmptyFields() {
        return emptyFields;
    }

    public void setEmptyFields(Integer emptyFields) {
        this.emptyFields = emptyFields;
    }

    public List<Object> getDependents() {
        return dependents;
    }

    public void setDependents(List<Object> dependents) {
        this.dependents = dependents;
    }
}