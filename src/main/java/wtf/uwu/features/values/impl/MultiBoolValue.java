package wtf.uwu.features.values.impl;

import wtf.uwu.features.modules.Module;
import wtf.uwu.features.values.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MultiBoolValue extends Value {
    public List<BoolValue> options;
    public int index;
    public float animation;

    public MultiBoolValue(String name, List<BoolValue> options, Module module, Supplier<Boolean> visible) {
        super(name, module, visible);
        this.options = options;
        this.index = options.size();
    }

    public MultiBoolValue(String name, List<BoolValue> options, Module module) {
        this(name, options, module, () -> true);
    }

    public boolean isEnabled(String name) {
        return this.options.stream()
                .filter(option -> option.getName().equalsIgnoreCase(name))
                .map(BoolValue::get)
                .findFirst()
                .orElse(false);
    }

    public void set(String name, boolean value) {
        this.options.stream()
                .filter(option -> option.getName().equalsIgnoreCase(name))
                .findFirst()
                .ifPresent(option -> option.set(value));
    }

    public List<BoolValue> getToggled() {
        return this.options.stream()
                .filter(BoolValue::get)
                .collect(Collectors.toList());
    }

    public String isEnabled() {
        List<String> includedOptions = new ArrayList<>();
        for (BoolValue option : options) {
            if (option.get()) {
                includedOptions.add(option.getName());
            }
        }
        return String.join(", ", includedOptions);
    }

    public void set(int index, boolean value) {
        if (index >= 0 && index < options.size()) {
            this.options.get(index).set(value);
        }
    }

    public boolean isEnabled(int index) {
        return index >= 0 && index < options.size() && this.options.get(index).get();
    }

    public List<BoolValue> getValues() {
        return this.options;
    }

    public void addIfMissing(String name, boolean defaultValue) {
        boolean exists = this.options.stream()
                .anyMatch(option -> option.getName().equalsIgnoreCase(name));

        if (!exists) {
            this.options.add(new BoolValue(name, defaultValue));
        }
    }
}
