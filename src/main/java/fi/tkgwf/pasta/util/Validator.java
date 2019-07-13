package fi.tkgwf.pasta.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

import fi.tkgwf.pasta.bean.Meta;
import fi.tkgwf.pasta.config.Config;

public class Validator {

    /**
     * Validate if a supplied text and meta are valid
     *
     * @param text A "paste"
     * @param newMeta The meta to be validated
     * @param existingMeta The existing meta that will be replaced by the new
     * meta, can be null
     * @return List of validation errors
     */
    public static List<String> validate(String text, Meta newMeta, Meta existingMeta) {
        return ListUtils.union(validateText(text), validateMeta(newMeta, existingMeta));
    }

    /**
     * Validate if a supplied meta is valid, optionally with an existing meta
     *
     * @param newMeta The meta to be validated
     * @param existingMeta The existing meta that will be replaced by the new
     * meta, can be null
     * @return List of validation errors
     */
    public static List<String> validateMeta(Meta newMeta, Meta existingMeta) {
        List<String> errors = new LinkedList<>();
        if (existingMeta != null) {
            if (!Objects.equals(existingMeta.id, newMeta.id)) {
                errors.add("Incorrect ID");
            }
            if (!Objects.equals(existingMeta.secret, newMeta.secret)) {
                errors.add("Wrong secret");
            }
        }
        if (newMeta.id != null && newMeta.id.length() < 5) {
            errors.add("ID is too short");
        }
        if (newMeta.secret != null && newMeta.secret.length() < 10) {
            errors.add("Secret is too short");
        }
        if (newMeta.expiry != null) {
            if (newMeta.expiry < Config.getLong("min_expiry", 60)) {
                errors.add("Expiry time is too short");
            }
            if (newMeta.expiry > Config.getLong("max_expiry", 31536000)) {
                errors.add("Expiry time is too long");
            }
        }
        return errors;
    }

    /**
     * Validate if a supplied text is a valid paste
     *
     * @param text A "paste"
     * @return List of validation errors
     */
    public static List<String> validateText(String text) {
        List<String> errors = new LinkedList<>();
        if (StringUtils.isEmpty(text)) {
            errors.add("No text supplied");
        }
        if (StringUtils.length(text) > Config.getLong("max_size", 1048576)) {
            errors.add("Text is too long");
        }
        return errors;
    }
}
