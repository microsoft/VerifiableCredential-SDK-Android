# Coding Guidelines

We follow a modified form of the [StandardJS](https://standardjs.com/) style guide. Additions and modifications have been made on this styleguide that will be aparent from linter output and our existing repos.

[![JavaScript Style Guide](https://img.shields.io/badge/code_style-standard-brightgreen.svg)](https://standardjs.com)

## Indentation & Lines
* We use spaces (2 to be exact), not tabs
* Lines must not be over 160 characters
* Lines must end in semicolons

## Names
* Use PascalCase for `type` names
* Use PascalCase for `enum` values
* Use camelCase for `function` and `method` names
* Use camelCase for `property` names and `local variables`
* Use whole words in names when possible
* Avoid prefixing names with underscore

## Types
* Do not export `types` or `functions` unless you need to share it across multiple components
* Do not introduce new `types` or `values` to the global namespace

## Comments
* Use JSDoc style comments for `functions`, `interfaces`, `enums`, and `classes`
* Favour clarity over brevity 

## Strings
* Use "double quotes" for strings shown to the user that need to be externalized (localized)
* Use 'single quotes' otherwise 
* All strings visible to the user need to be externalized

## Style
* Use arrow functions `=>` over anonymous function expressions
* Only surround arrow function parameters when necessary. For example, `(x) => x + x` is wrong but the following are correct:
``` javascript
  x => x + x
  (x,y) => x + y
  <T>(x: T, y: T) => x === y
```
* Always surround loop and conditional bodies with curly braces
* Open curly braces always go on the same line as whatever necessitates them
* Parenthesized constructs should have no surrounding whitespace. A single space follows commas, colons, and semicolons in those constructs. For example:
``` javascript
  for (var i = 0, n = str.length; i < 10; i++) { }
  if (x < 10) { }
  function f(x: number, y: string): void { }
```
* Keys in objects must remain consistent, but prefer no quotes.
```typescript
  {
      foo: 1,
      bar: 'string',
      baz: true
  }
  {
      'foo': 1,
      'bar': 'string',
      'baz-1': true
  }
```
