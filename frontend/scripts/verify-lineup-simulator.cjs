/* Run with Node 22. Exercises the real component class without a browser or a backend. */
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const root = path.resolve(__dirname, '../..');
const ts = require(path.join(root, 'frontend/node_modules/typescript'));
const file = path.join(root, 'frontend/src/app/lineup-simulator/lineup-simulator.component.ts');
const js = ts.transpileModule(fs.readFileSync(file, 'utf8'), {
  compilerOptions: {target: ts.ScriptTarget.ES2022, module: ts.ModuleKind.CommonJS, experimentalDecorators: true}
}).outputText;
const storage = new Map();
const sandbox = {
  exports: {}, setTimeout, clearTimeout,
  localStorage: {getItem: key => storage.get(key) ?? null, setItem: (key, value) => storage.set(key, value)},
  require: name => {
    if (name === '@angular/core') return {Component: () => klass => klass, Input: () => () => {}};
    if (name === '@angular/common') return {CommonModule: {}};
    if (name === '@angular/forms') return {FormsModule: {}};
    throw new Error(`Unexpected runtime import ${name}`);
  }
};
vm.runInNewContext(js, sandbox, {filename: file});
const Component = sandbox.exports.LineupSimulatorComponent;
const calls = [];
const service = {rateLineup: request => ({subscribe: observer => {
  const call = {request, observer, cancelled: false}; calls.push(call);
  return {unsubscribe: () => {call.cancelled = true;}};
}})};
const c = new Component(service);
const makePlayer = id => ({...c.customPlayer, id, firstName: `Jugador ${id}`, lastName: `Apellido ${id}`, playerNumber: id, keeperSkill: id, playerForm: 5});
c.players = Array.from({length: 20}, (_, i) => makePlayer(i + 1));
c.ngOnChanges({players: {}});
let checks = 0;
const test = (label, work) => {work(); checks++; console.log(`PASS ${label}`);};
const clear = () => {c.slots.forEach(s => {s.playerId = null; s.behaviour = 0;}); c.bench.forEach(b => b.playerId = null); c.captainId = null; c.setPieceTakerId = null;};
try {
  test('empty XI has the original 0.75 sector baseline', () => assert.ok(c.ratingValues.every(value => value === 0.75)));
  test('enforces eleven players without losing the existing XI', () => {
    c.slots.slice(0,11).forEach((slot,i) => c.assign(slot,i+1));
    c.assign(c.slots[11],12); assert.equal(c.fieldCount,11); assert.equal(c.slots[11].playerId,null);
  });
  test('swaps field players and keeps destination behaviours', () => {
    c.slots[1].behaviour=2; c.slots[2].behaviour=1;
    c.assign(c.slots[2],c.slots[1].playerId);
    assert.equal(c.slots[1].playerId,3); assert.equal(c.slots[2].playerId,2);
    assert.equal(c.slots[1].behaviour,2); assert.equal(c.slots[2].behaviour,1); assert.equal(c.fieldCount,11);
  });
  test('disallows unsupported behaviour/position combinations', () => {c.assign(c.slots[0],1,4); assert.equal(c.slots[0].behaviour,0);});
  test('backup bench requires a primary substitute in another role', () => {
    clear(); c.assignBench(c.bench[7],15); assert.equal(c.bench[7].playerId,null);
    c.assignBench(c.bench[0],15); c.assignBench(c.bench[7],15); assert.equal(c.bench[7].playerId,null);
    c.assignBench(c.bench[8],15); assert.equal(c.bench[8].playerId,15);
    c.removeBench(c.bench[0]); assert.equal(c.bench[8].playerId,null);
  });
  test('moving a substitute onto the field clears all its bench references', () => {
    clear(); c.assignBench(c.bench[0],15); c.assignBench(c.bench[1],15); c.assignBench(c.bench[9],15);
    c.assign(c.slots[0],15); assert.ok(c.bench.every(b => b.playerId !== 15));
  });
  test('mirroring is reversible for assignments and behaviours', () => {
    clear(); c.assign(c.slots[1],1,2); c.assign(c.slots[5],2,1);
    c.mirrorLineup(); assert.equal(c.slots[1].playerId,2); assert.equal(c.slots[1].behaviour,1);
    c.mirrorLineup(); assert.equal(c.slots[1].playerId,1); assert.equal(c.slots[1].behaviour,2);
  });
  test('changing formation preserves substitutes', () => {c.assignBench(c.bench[0],15); c.chooseFormation('3-5-2'); assert.equal(c.bench[0].playerId,15);});
  test('rating request maps roles, behaviours and match context', () => {
    clear(); c.assign(c.slots[0],1); c.assign(c.slots[7],2,1);
    c.styleOfPlay=-7; c.venue=1; c.spirit=8; c.confidence=6; c.teamAttitude='MOTS'; c.tactic='PRESSING'; c.recalculate();
    const req=calls.at(-1).request;
    assert.equal(req.fieldPlayers.length,2); assert.equal(req.fieldPlayers[1].role,'RIGHT_INNER_MIDFIELD'); assert.equal(req.fieldPlayers[1].behaviour,'OFFENSIVE');
    assert.equal(req.matchDetail.styleOfPlay,-70); assert.equal(req.matchDetail.sideMatch,'HOME'); assert.equal(req.matchDetail.teamSpirit,'DELIRIOUS'); assert.equal(req.matchDetail.teamConfidence,'WONDERFUL');
    assert.equal(req.benchPlayers.length,0);
  });
  test('stale rating responses cannot overwrite the current lineup', () => {
    const older=calls.at(-1); c.styleOfPlay=3; c.scheduleCalculation(); assert.equal(older.cancelled,true); c.recalculate();
    const newer=calls.at(-1); const values={rightDefense:3,centralDefense:4,leftDefense:5,midfield:6,rightAttack:7,centralAttack:8,leftAttack:9};
    newer.observer.next(values); older.observer.next({...values,rightDefense:99}); assert.equal(c.ratings.rightDefense,3);
  });
  test('failed predictions clear outdated results', () => {c.recalculate(); calls.at(-1).observer.error(new Error('offline')); assert.equal(c.ratings,null); assert.equal(c.error,true); assert.equal(c.calculating,false);});
  test('memory restores assignments and context', () => {
    c.error=false; c.calculating=false; c.saveMemory(); const id=c.slots[0].playerId; c.removeSlot(c.slots[0]); c.venue=2; c.restoreMemory(); assert.equal(c.slots[0].playerId,id); assert.equal(c.venue,1);
  });
  test('custom players are unique and belong only to the simulator', () => {
    c.startCustomPlayer(); c.customPlayer.firstName='Prueba'; c.customPlayer.lastName='Uno'; c.saveCustomPlayer();
    c.startCustomPlayer(); c.customPlayer.firstName='Prueba'; c.customPlayer.lastName='Dos'; c.saveCustomPlayer();
    assert.equal(c.customPlayers.length,2); assert.notEqual(c.customPlayers[0].id,c.customPlayers[1].id); assert.equal(c.players.length,20);
  });
  test('shirt sorting puts unnumbered players after numbered players', () => {c.players[0].playerNumber=undefined;c.sortBy='playerNumber';assert.equal(c.sortedPlayers[0].playerNumber,2);});
  test('form dots exactly represent the observed 5/8 form', () => {const p=makePlayer(1); assert.equal(c.formClass(p),'form-high');assert.equal(Array.from({length:4},(_,i)=>c.formDot(p,i)).join(','),'empty,half,full,full');});
  test('importing a projected week loads players and its formation', () => {
    const p=makePlayer(50);c.planner={teamTrainingResponse:{weekPlayers:{4:[p]},weekFormationRatings:{4:{players:[{playerId:50,position:{role:'KEEPER',behaviour:'NORMAL'}}]}}}};c.importWeek=4;c.importPlan();
    assert.equal(c.player(50).id,50);assert.equal(c.slots[0].playerId,50);assert.equal(c.showImportPlan,false);
  });
  test('switching project cannot leak the previous match context', () => {
    c.tactic='PRESSING';c.venue=1;c.styleOfPlay=-10;c.storageKey='ehm-lineup:another-project';c.ngOnChanges({storageKey:{}});
    assert.equal(c.tactic,'NORMAL');assert.equal(c.venue,2);assert.equal(c.styleOfPlay,0);assert.equal(c.fieldCount,0);assert.equal(c.ratingMemory,null);
  });
  console.log(`${checks} simulator state checks passed.`);
} finally {c.ngOnDestroy();}
