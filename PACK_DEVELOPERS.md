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

# Item Models

## `rpt:template` item model
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
  "model": {
    "type": "model",
    "model": "item/diamond"
  },
  "rpt": {
    ...
  }
}
```

## `rpt:variable` item model
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

## `rpt:regex` item model
if `minecraft/items` folder: 
```json
{
  "model": {
    "type": "rpt:regex",
    "cases": [
      {
        "when": ["^Hello ", "^Привет"],
        "model": ...
      },
      {
        "when": "123",
        "model": ...
      }
    ]

    }
  }
}
```

# Conditional item model
todo: доделать

## `rpt:has_flag` property
```json
"flag": "123"
```

## `rpt:match`
```json
"regex": "some_regex"
```
