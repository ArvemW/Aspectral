package arvem.aspectral.layer;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a layer that can have one aspect assigned to it.
 * Multiple layers allow for aspects + classes + sub-aspects, etc.
 * <p>
 * Inspired by Origins' layer system which allows one origin per layer.
 */
public class Layer {
    private final String id;
    private final int order;
    private final boolean enabled;
    private final List<String> aspects;
    private final boolean allowRandom;
    private final List<String> excludeRandom;
    private final boolean allowRandomUnchoosable;
    private final boolean hidden;
    private final String name;
    private final boolean replace;

    public Layer(String id, JsonObject json) {
        this.id = id;
        this.order = json.has("order") ? json.get("order").getAsInt() : 0;
        this.enabled = !json.has("enabled") || json.get("enabled").getAsBoolean();
        this.allowRandom = !json.has("allow_random") || json.get("allow_random").getAsBoolean();
        this.allowRandomUnchoosable = json.has("allow_random_unchoosable") && json.get("allow_random_unchoosable").getAsBoolean();
        this.hidden = json.has("hidden") && json.get("hidden").getAsBoolean();
        this.name = json.has("name") ? json.get("name").getAsString() : id;
        this.replace = json.has("replace") && json.get("replace").getAsBoolean();

        this.aspects = new ArrayList<>();
        if (json.has("aspects") && json.get("aspects").isJsonArray()) {
            json.get("aspects").getAsJsonArray().forEach(elem -> aspects.add(elem.getAsString()));
        }

        this.excludeRandom = new ArrayList<>();
        if (json.has("exclude_random") && json.get("exclude_random").isJsonArray()) {
            json.get("exclude_random").getAsJsonArray().forEach(elem -> excludeRandom.add(elem.getAsString()));
        }
    }

    public String getId() {
        return id;
    }

    public int getOrder() {
        return order;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public List<String> getAspects() {
        return new ArrayList<>(aspects);
    }

    public boolean isAllowRandom() {
        return allowRandom;
    }

    public List<String> getExcludeRandom() {
        return new ArrayList<>(excludeRandom);
    }

    public boolean isAllowRandomUnchoosable() {
        return allowRandomUnchoosable;
    }

    public boolean isHidden() {
        return hidden;
    }

    public String getName() {
        return name;
    }

    public boolean isReplace() {
        return replace;
    }

    /**
     * Check if an aspect is allowed in this layer.
     */
    public boolean isAspectAllowed(String aspectId) {
        return enabled && aspects.contains(aspectId);
    }

    /**
     * Check if an aspect can be randomly selected.
     */
    public boolean canRandomlySelect(String aspectId) {
        return allowRandom && !excludeRandom.contains(aspectId);
    }
}

