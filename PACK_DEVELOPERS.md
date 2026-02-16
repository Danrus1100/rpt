# `rpt` field

```json
{
  "model": {
    ...
  },
  "rpt": {
    "custom_flags": [
      "foo",
      "bar"
    ],
    "variables": {
      "strings": {
        "foo": "bar"
      },
      "numbers": {
        "foo": 42
      },
      "flags": {
        "hello_world": true
      },
      "models": {
        "bar": "minecraft:iron_ingot"
      }
    }
  }
}
```

# `rpt:template` item model
in `minecraft/items` folder:
```json
{
  "model": {
    "type": "rpt:template",
    "template": "namespace:my_template"
  }
}
```
in `namespace/rpt/templates` folder:
```json
{
  "type": "model",
  "model": "item/diamond"
}
```

# `rpt:variable` item model
in `minecraft/items` folder:
```json
{
  "model": {
    "type": "rpt:variable",
    "variable": "foo"
  },
  "rpt": {
    "variables" : {
      "models": {
        "foo": "item/diamond"
      }
    }
  }
}
```