package net.optifine.util;

import java.util.Arrays;

public class CacheLocal
{
    private final int maxX;
    private final int maxY;
    private final int maxZ;
    private final int maxYZ; // Pre-calculado para optimizar indexing
    private final int totalSize; // Tamaño total del array 1D

    private int offsetX = 0;
    private int offsetY = 0;
    private int offsetZ = 0;

    // Optimización: usar array 1D en vez de 3D para mejor cache locality
    private int[] cache = null;

    // Cache para la última query - optimización para accesos consecutivos
    private int lastIndex = -1;
    private int lastX = Integer.MIN_VALUE;
    private int lastY = Integer.MIN_VALUE;
    private int lastZ = Integer.MIN_VALUE;

    // Pool de arrays para reducir garbage collection
    private static final int POOL_SIZE = 8;
    private static final CacheLocal[] CACHE_POOL = new CacheLocal[POOL_SIZE];
    private static int poolIndex = 0;

    public CacheLocal(int maxX, int maxY, int maxZ)
    {
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        this.maxYZ = maxY * maxZ; // Pre-calcular para indexing rápido
        this.totalSize = maxX * maxY * maxZ;
        this.cache = new int[this.totalSize];
        this.resetCache();
    }

    // Factory method optimizado con object pooling
    public static CacheLocal getInstance(int maxX, int maxY, int maxZ)
    {
        // Intentar reutilizar cache del pool si las dimensiones coinciden
        synchronized (CACHE_POOL)
        {
            for (int i = 0; i < POOL_SIZE; i++)
            {
                CacheLocal cached = CACHE_POOL[i];
                if (cached != null && cached.maxX == maxX &&
                        cached.maxY == maxY && cached.maxZ == maxZ)
                {
                    CACHE_POOL[i] = null; // Remover del pool
                    cached.resetCache();
                    return cached;
                }
            }
        }

        return new CacheLocal(maxX, maxY, maxZ);
    }

    // Devolver cache al pool para reutilización
    public void release()
    {
        synchronized (CACHE_POOL)
        {
            CACHE_POOL[poolIndex] = this;
            poolIndex = (poolIndex + 1) % POOL_SIZE;
        }
    }

    // Optimización: usar Arrays.fill que es nativo y súper rápido
    public void resetCache()
    {
        Arrays.fill(this.cache, -1);

        // Reset del cache de última query
        this.lastIndex = -1;
        this.lastX = Integer.MIN_VALUE;
        this.lastY = Integer.MIN_VALUE;
        this.lastZ = Integer.MIN_VALUE;
    }

    // Versión optimizada que solo resetea una región específica
    public void resetCacheRegion(int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
    {
        // Clamp valores a los bounds del cache
        minX = Math.max(0, minX - offsetX);
        minY = Math.max(0, minY - offsetY);
        minZ = Math.max(0, minZ - offsetZ);
        maxX = Math.min(this.maxX, maxX - offsetX + 1);
        maxY = Math.min(this.maxY, maxY - offsetY + 1);
        maxZ = Math.min(this.maxZ, maxZ - offsetZ + 1);

        for (int x = minX; x < maxX; x++)
        {
            for (int y = minY; y < maxY; y++)
            {
                int startIndex = getIndex(x, y, minZ);
                int length = maxZ - minZ;
                Arrays.fill(cache, startIndex, startIndex + length, -1);
            }
        }

        // Invalidar cache de última query si está en la región
        if (lastX >= minX + offsetX && lastX < maxX + offsetX &&
                lastY >= minY + offsetY && lastY < maxY + offsetY &&
                lastZ >= minZ + offsetZ && lastZ < maxZ + offsetZ)
        {
            lastIndex = -1;
        }
    }

    public void setOffset(int x, int y, int z)
    {
        // Solo resetear si el offset realmente cambió
        if (this.offsetX != x || this.offsetY != y || this.offsetZ != z)
        {
            this.offsetX = x;
            this.offsetY = y;
            this.offsetZ = z;
            this.resetCache();
        }
    }

    // Versión optimizada de setOffset que permite mantener datos válidos
    public void setOffsetSmart(int x, int y, int z)
    {
        int deltaX = x - this.offsetX;
        int deltaY = y - this.offsetY;
        int deltaZ = z - this.offsetZ;

        // Si el offset no cambió, no hacer nada
        if (deltaX == 0 && deltaY == 0 && deltaZ == 0)
        {
            return;
        }

        // Si el cambio es muy grande, mejor resetear todo
        if (Math.abs(deltaX) >= maxX || Math.abs(deltaY) >= maxY || Math.abs(deltaZ) >= maxZ)
        {
            this.offsetX = x;
            this.offsetY = y;
            this.offsetZ = z;
            this.resetCache();
            return;
        }

        // TODO: Implementar shifting inteligente del cache
        // Por ahora, usar método simple
        this.offsetX = x;
        this.offsetY = y;
        this.offsetZ = z;
        this.resetCache();
    }

    // Calcular índice 1D de forma optimizada
    private int getIndex(int x, int y, int z)
    {
        return x * maxYZ + y * maxZ + z;
    }

    // Verificar bounds de forma optimizada
    private boolean isInBounds(int x, int y, int z)
    {
        return x >= 0 && x < maxX && y >= 0 && y < maxY && z >= 0 && z < maxZ;
    }

    public int get(int x, int y, int z)
    {
        // Optimización: cache de última query para accesos consecutivos
        if (x == lastX && y == lastY && z == lastZ && lastIndex != -1)
        {
            return cache[lastIndex];
        }

        // Calcular coordenadas relativas
        int relX = x - this.offsetX;
        int relY = y - this.offsetY;
        int relZ = z - this.offsetZ;

        // Bounds check optimizado
        if (!isInBounds(relX, relY, relZ))
        {
            return -1; // No imprimir stack trace por performance
        }

        // Calcular índice y obtener valor
        int index = getIndex(relX, relY, relZ);
        int value = cache[index];

        // Actualizar cache de última query
        lastX = x;
        lastY = y;
        lastZ = z;
        lastIndex = index;

        return value;
    }

    // Versión unsafe para casos donde sabemos que está in-bounds
    public int getUnsafe(int x, int y, int z)
    {
        int relX = x - this.offsetX;
        int relY = y - this.offsetY;
        int relZ = z - this.offsetZ;

        int index = getIndex(relX, relY, relZ);
        return cache[index];
    }

    public void set(int x, int y, int z, int val)
    {
        int relX = x - this.offsetX;
        int relY = y - this.offsetY;
        int relZ = z - this.offsetZ;

        if (!isInBounds(relX, relY, relZ))
        {
            return;
        }

        int index = getIndex(relX, relY, relZ);
        cache[index] = val;

        // Actualizar cache de última query si es la misma posición
        if (x == lastX && y == lastY && z == lastZ)
        {
            lastIndex = index;
        }
    }

    // Versión unsafe para casos donde sabemos que está in-bounds
    public void setUnsafe(int x, int y, int z, int val)
    {
        int relX = x - this.offsetX;
        int relY = y - this.offsetY;
        int relZ = z - this.offsetZ;

        int index = getIndex(relX, relY, relZ);
        cache[index] = val;
    }

    // Optimización: setLast usando el cache de índice
    public void setLast(int val)
    {
        if (lastIndex != -1)
        {
            cache[lastIndex] = val;
        }
    }

    // Batch operations para mejor performance
    public void setRegion(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, int val)
    {
        for (int x = minX; x <= maxX; x++)
        {
            for (int y = minY; y <= maxY; y++)
            {
                for (int z = minZ; z <= maxZ; z++)
                {
                    set(x, y, z, val);
                }
            }
        }
    }

    // Prefetch para mejorar cache locality
    public void prefetch(int centerX, int centerY, int centerZ, int radius)
    {
        int minX = centerX - radius;
        int maxX = centerX + radius;
        int minY = centerY - radius;
        int maxY = centerY + radius;
        int minZ = centerZ - radius;
        int maxZ = centerZ + radius;

        // Acceder a las posiciones en orden de memoria para warm-up del cache
        for (int x = minX; x <= maxX; x++)
        {
            for (int y = minY; y <= maxY; y++)
            {
                for (int z = minZ; z <= maxZ; z++)
                {
                    get(x, y, z); // Esto carga las líneas de cache
                }
            }
        }
    }

    // Estadísticas de uso para debugging
    private int hits = 0;
    private int misses = 0;

    public void recordHit() { hits++; }
    public void recordMiss() { misses++; }

    public double getHitRatio()
    {
        int total = hits + misses;
        return total > 0 ? (double)hits / total : 0.0;
    }

    public void printStats()
    {
        System.out.println("CacheLocal Stats - Hits: " + hits +
                ", Misses: " + misses +
                ", Hit Rate: " + String.format("%.2f%%", getHitRatio() * 100));
    }

    // Getters para debugging
    public int getMaxX() { return maxX; }
    public int getMaxY() { return maxY; }
    public int getMaxZ() { return maxZ; }
    public int getOffsetX() { return offsetX; }
    public int getOffsetY() { return offsetY; }
    public int getOffsetZ() { return offsetZ; }
}