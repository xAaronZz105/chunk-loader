package de.xaaronzz105.chunkLoader;

import org.bukkit.Particle;
import org.bukkit.util.Vector;

public record ParticleData(Particle particle, int particleAmount, Vector delta, double speed) {
}
