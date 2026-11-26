import {TruncateDecimalsPipe} from './truncate-decimals.pipe';

describe('Decimal display precision', () => {
  const pipe = new TruncateDecimalsPipe();
  it('preserves every manually entered hundredth at every player skill level', () => {
    for (let level = 0; level <= 20; level++) {
      for (let sub = 0; sub < 100; sub++) {
        const expected = `${level}.${String(sub).padStart(2, '0')}`;
        expect(pipe.transform(Number(expected), 2)).toBe(expected);
      }
    }
  });
  it('truncates computed values without rounding up to an unobserved skill level', () => {
    expect(pipe.transform(6.999999999999999, 2)).toBe('6.99');
    expect(pipe.transform(0.35 / 100, 2)).toBe('0.00');
    expect(pipe.transform(-6.9999, 2)).toBe('-6.99');
    expect(pipe.transform(1e-7, 2)).toBe('0.00');
    expect(pipe.transform(1e21, 2)).toBe('1000000000000000000000.00');
  });
});
