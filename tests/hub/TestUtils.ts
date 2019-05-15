import * as _ from 'lodash';

/**
 * Returns a clone of the given object with certain fields changed.
 *
 * @param original The original object to clone and modify.
 * @param adjustments An object containing fields to be replaced. Each key of the object is a field
 * to change, in dotted notation (e.g. `field` or `field.prop`). The value is the new value to set
 * for that key.
 */
export const alter = (original: any, adjustments: {[fieldPath: string]: any}) => {
  const clone = _.cloneDeep(original);
  Object.keys(adjustments).forEach((fieldPath) => {
    const newValue = adjustments[fieldPath];
    (newValue !== undefined)
      ? _.set(clone, fieldPath, newValue)
      : _.unset(clone, fieldPath);
  });
  return clone;
};

/**
 * Returns a debug description of the given value.
 */
export const explain = (value: any): string => {
  if (Array.isArray(value)) {
    let joined = (value).map(item => explain(item)).join(',');
    return `[${joined}]`;
  } else if (typeof value === 'string') {
    return `'${value}'`;
  } else if (value === null) {
    return 'null';
  } else if (value === undefined) {
    return 'undefined';
  } else if (typeof value === 'object') {
    return JSON.stringify(value);
  }
  return value.toString();
};