package sim;

public class Weapon {

		final int aimRange;
		final int minRange;
		final int autoRange;
		final int snapRange;
		final double dropOff;
		final int dmg;
		public Weapon(int aimRange, int minRange, int autoRange, int snapRange, double dropOff, int dmg) {
			this.aimRange = aimRange;
			this.minRange = minRange;
			this.autoRange = autoRange;
			this.snapRange = snapRange;
			this.dropOff = dropOff;
			this.dmg = dmg;
		}
}
