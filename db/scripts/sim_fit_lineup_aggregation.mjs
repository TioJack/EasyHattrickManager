#!/usr/bin/env node
/*
 * Fit only on full-lineup-aggregation-v1 and score only against the held-out
 * XIs. The input feature is the empirical no-fit composition from the
 * single-player matrix; no legacy rating code is consulted.
 */
import {execFileSync} from 'node:child_process';

function load(campaign) {
  return JSON.parse(execFileSync(process.execPath, ['db/scripts/sim_validate_full_lineups.mjs', '--campaign', campaign], {
    cwd: process.cwd(), encoding: 'utf8', maxBuffer: 64 * 1024 * 1024, env: process.env
  }));
}
function solve(matrix, vector) {
  const n = vector.length;
  const augmented = matrix.map((row, index) => [...row, vector[index]]);
  for (let column = 0; column < n; column++) {
    let pivot = column;
    for (let row = column + 1; row < n; row++) if (Math.abs(augmented[row][column]) > Math.abs(augmented[pivot][column])) pivot = row;
    if (Math.abs(augmented[pivot][column]) < 1e-9) throw new Error('Aggregation design matrix is singular.');
    [augmented[column], augmented[pivot]] = [augmented[pivot], augmented[column]];
    const divisor = augmented[column][column];
    for (let item = column; item <= n; item++) augmented[column][item] /= divisor;
    for (let row = 0; row < n; row++) if (row !== column) {
      const factor = augmented[row][column];
      for (let item = column; item <= n; item++) augmented[row][item] -= factor * augmented[column][item];
    }
  }
  return augmented.map(row => row[n]);
}
function features(item, sector) {
  const formation = item.formation;
  return [1, item.byMinute[0].predicted[sector], formation === '3-5-2' ? 1 : 0, formation === '5-3-2' ? 1 : 0];
}
function dot(left, right) { return left.reduce((sum, value, index) => sum + value * right[index], 0); }
function mae(values) { return values.reduce((sum, value) => sum + Math.abs(value), 0) / values.length; }

const training = load('full-lineup-aggregation-v1');
const holdout = load('full-lineup-holdout-v1');
if (training.summary.missingComponents || holdout.summary.missingComponents) throw new Error('Cannot fit with missing single-player components.');

const coefficients = [];
for (let sector = 0; sector < 7; sector++) {
  const normal = Array.from({length: 4}, () => Array(4).fill(0));
  const right = Array(4).fill(0);
  for (const item of training.validations) {
    const x = features(item, sector), y = item.byMinute[0].actual[sector];
    for (let row = 0; row < 4; row++) {
      right[row] += x[row] * y;
      for (let column = 0; column < 4; column++) normal[row][column] += x[row] * x[column];
    }
  }
  coefficients.push(solve(normal, right));
}

const score = dataset => {
  const rawResiduals = [], calibratedResiduals = [], roundedResiduals = [];
  for (const item of dataset.validations) for (let sector = 0; sector < 7; sector++) {
    const actual = item.byMinute[0].actual[sector];
    const raw = item.byMinute[0].predicted[sector];
    const calibrated = dot(coefficients[sector], features(item, sector));
    rawResiduals.push(actual - raw);
    calibratedResiduals.push(actual - calibrated);
    roundedResiduals.push(actual - Math.round(calibrated));
  }
  return {
    values: rawResiduals.length,
    rawMae: Number(mae(rawResiduals).toFixed(3)),
    calibratedMae: Number(mae(calibratedResiduals).toFixed(3)),
    roundedMae: Number(mae(roundedResiduals).toFixed(3)),
    calibratedMaxAbsoluteError: Number(Math.max(...calibratedResiduals.map(Math.abs)).toFixed(3)),
    roundedMaxAbsoluteError: Math.max(...roundedResiduals.map(Math.abs))
  };
};
console.log(JSON.stringify({
  generatedAt: new Date().toISOString(),
  modelVersion: 'v2-aggregation-affine-1',
  features: ['intercept', 'same-sector empirical single-player composition', 'is 3-5-2', 'is 5-3-2'],
  coefficients: coefficients.map((value, sector) => ({sector, intercept: value[0], composition: value[1], formation3_5_2: value[2], formation5_3_2: value[3]})),
  training: score(training),
  heldOutValidation: score(holdout),
  caveat: 'This is a measured aggregation candidate, not a production formula: it has only three normal-behaviour formations and fixed match context. Keep V2 in CALIBRATING until contextual and substitution validation is complete.'
}, null, 2));
