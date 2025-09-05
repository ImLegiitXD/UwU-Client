package net.optifine.render;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public class ChunkVisibility
{
    public static final int MASK_FACINGS = 63;
    public static final EnumFacing[][] enumFacingArrays = makeEnumFacingArrays(false);
    public static final EnumFacing[][] enumFacingOppositeArrays = makeEnumFacingArrays(true);

    // Optimización: usar AtomicInteger para thread-safety sin locks
    private static final AtomicInteger counter = new AtomicInteger(0);
    private static volatile int iMaxStatic = -1;
    private static volatile int iMaxStaticFinal = 16;
    private static volatile World worldLast = null;
    private static volatile int pcxLast = Integer.MIN_VALUE;
    private static volatile int pczLast = Integer.MIN_VALUE;

    // Cache para chunks ya procesados
    private static final int CACHE_SIZE = 256;
    private static final ChunkHeightCache[] heightCache = new ChunkHeightCache[CACHE_SIZE];
    private static int cacheHits = 0;
    private static int cacheMisses = 0;

    // Estructura para cachear alturas de chunks
    private static class ChunkHeightCache {
        long chunkKey;
        int maxHeight;
        long lastAccess;

        ChunkHeightCache(long key, int height) {
            this.chunkKey = key;
            this.maxHeight = height;
            this.lastAccess = System.currentTimeMillis();
        }
    }

    // Generar key única para un chunk
    private static long getChunkKey(int x, int z) {
        return ((long)x << 32) | (z & 0xFFFFFFFFL);
    }

    // Hash function optimizada para el cache
    private static int getCacheIndex(long chunkKey) {
        return (int)(chunkKey ^ (chunkKey >>> 32)) & (CACHE_SIZE - 1);
    }

    public static int getMaxChunkY(World world, Entity viewEntity, int renderDistanceChunks)
    {
        // Optimización: usar bit shifts más eficientes
        int playerChunkX = (int)(viewEntity.posX) >> 4;
        int playerChunkY = (int)(viewEntity.posY) >> 4;
        int playerChunkZ = (int)(viewEntity.posZ) >> 4;

        // Early exit si estamos fuera del mundo
        if (playerChunkY < 0 || playerChunkY > 15) {
            return Math.max(0, Math.min(255, playerChunkY << 4));
        }

        Chunk playerChunk = world.getChunkFromChunkCoords(playerChunkX, playerChunkZ);

        // Calcular bounds una sola vez
        int minChunkX = playerChunkX - renderDistanceChunks;
        int maxChunkX = playerChunkX + renderDistanceChunks;
        int minChunkZ = playerChunkZ - renderDistanceChunks;
        int maxChunkZ = playerChunkZ + renderDistanceChunks;

        // Reset cache si cambiamos de posición significativamente
        if (world != worldLast || playerChunkX != pcxLast || playerChunkZ != pczLast)
        {
            counter.set(0);
            iMaxStaticFinal = 16;
            worldLast = world;
            pcxLast = playerChunkX;
            pczLast = playerChunkZ;

            // Limpiar cache viejo si es necesario
            clearOldCacheEntries();
        }

        int currentCounter = counter.get();
        if (currentCounter == 0)
        {
            iMaxStatic = -1;
        }

        int maxHeight = iMaxStatic;

        // Optimización: procesar chunks en un patrón más eficiente
        int startX = minChunkX, endX = maxChunkX + 1;
        int startZ = minChunkZ, endZ = maxChunkZ + 1;

        switch (currentCounter)
        {
            case 0: // Cuadrante NE
                endX = playerChunkX + 1;
                endZ = playerChunkZ + 1;
                break;
            case 1: // Cuadrante NW
                startX = playerChunkX;
                endZ = playerChunkZ + 1;
                break;
            case 2: // Cuadrante SE
                endX = playerChunkX + 1;
                startZ = playerChunkZ;
                break;
            case 3: // Cuadrante SW
                startX = playerChunkX;
                startZ = playerChunkZ;
                break;
        }

        // Procesar chunks en el orden más eficiente (spiral desde el centro)
        maxHeight = processChunksOptimized(world, playerChunk, playerChunkY,
                startX, endX, startZ, endZ, maxHeight);

        // Actualizar contadores y estado
        if (currentCounter < 3)
        {
            iMaxStatic = maxHeight;
            maxHeight = iMaxStaticFinal;
        }
        else
        {
            iMaxStaticFinal = maxHeight;
            iMaxStatic = -1;
        }

        counter.compareAndSet(currentCounter, (currentCounter + 1) & 3); // Usar bitwise para modulo
        return maxHeight << 4;
    }

    private static int processChunksOptimized(World world, Chunk playerChunk, int playerChunkY,
                                              int startX, int endX, int startZ, int endZ, int currentMax)
    {
        int maxHeight = currentMax;

        for (int chunkX = startX; chunkX < endX; ++chunkX)
        {
            for (int chunkZ = startZ; chunkZ < endZ; ++chunkZ)
            {
                long chunkKey = getChunkKey(chunkX, chunkZ);
                int cacheIndex = getCacheIndex(chunkKey);

                // Intentar usar cache primero
                ChunkHeightCache cached = heightCache[cacheIndex];
                if (cached != null && cached.chunkKey == chunkKey)
                {
                    // Cache hit!
                    cacheHits++;
                    cached.lastAccess = System.currentTimeMillis();
                    if (cached.maxHeight > maxHeight)
                    {
                        maxHeight = cached.maxHeight;
                    }
                    continue;
                }

                // Cache miss - calcular altura
                cacheMisses++;
                Chunk chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
                int chunkMaxHeight = calculateChunkMaxHeight(chunk, playerChunk, playerChunkY, currentMax);

                // Guardar en cache
                heightCache[cacheIndex] = new ChunkHeightCache(chunkKey, chunkMaxHeight);

                if (chunkMaxHeight > maxHeight)
                {
                    maxHeight = chunkMaxHeight;
                }
            }
        }

        return maxHeight;
    }

    private static int calculateChunkMaxHeight(Chunk chunk, Chunk playerChunk, int playerChunkY, int currentMax)
    {
        if (chunk.isEmpty())
        {
            return currentMax;
        }

        int localMax = currentMax;
        ExtendedBlockStorage[] blockStorages = chunk.getBlockStorageArray();

        // Optimización: empezar desde arriba y parar en cuanto encontremos algo
        for (int y = blockStorages.length - 1; y > localMax; --y)
        {
            ExtendedBlockStorage storage = blockStorages[y];
            if (storage != null && !storage.isEmpty())
            {
                localMax = y;
                break; // Early exit - encontramos el bloque más alto
            }
        }

        // Procesar tile entities de forma más eficiente
        localMax = Math.max(localMax, processTileEntities(chunk, localMax));

        // Procesar entidades solo si es necesario
        localMax = Math.max(localMax, processEntities(chunk, playerChunk, playerChunkY, localMax));

        return localMax;
    }

    private static int processTileEntities(Chunk chunk, int currentMax)
    {
        int maxHeight = currentMax;

        try
        {
            Map<BlockPos, TileEntity> tileEntityMap = chunk.getTileEntityMap();
            if (tileEntityMap.isEmpty())
            {
                return maxHeight;
            }

            // Optimización: iterar sobre values en vez de keySet para evitar hash lookups
            for (TileEntity tileEntity : tileEntityMap.values())
            {
                BlockPos pos = tileEntity.getPos();
                if (pos != null)
                {
                    int tileY = pos.getY() >> 4;
                    if (tileY > maxHeight)
                    {
                        maxHeight = tileY;
                    }
                }
            }
        }
        catch (ConcurrentModificationException e)
        {
            // Ignorar - chunk siendo modificado concurrentemente
        }

        return maxHeight;
    }

    private static int processEntities(Chunk chunk, Chunk playerChunk, int playerChunkY, int currentMax)
    {
        int maxHeight = currentMax;
        ClassInheritanceMultiMap<Entity>[] entityLists = chunk.getEntityLists();

        for (int y = entityLists.length - 1; y > maxHeight; --y)
        {
            ClassInheritanceMultiMap<Entity> entityList = entityLists[y];

            if (!entityList.isEmpty())
            {
                // Optimización: skip si es solo el player en su propio chunk
                if (chunk == playerChunk && y == playerChunkY && entityList.size() == 1)
                {
                    continue;
                }

                maxHeight = y;
                break; // Early exit
            }
        }

        return maxHeight;
    }

    // Limpiar entradas viejas del cache periodicamente
    private static void clearOldCacheEntries()
    {
        long currentTime = System.currentTimeMillis();
        long maxAge = 5000; // 5 segundos

        for (int i = 0; i < CACHE_SIZE; i++)
        {
            ChunkHeightCache entry = heightCache[i];
            if (entry != null && (currentTime - entry.lastAccess) > maxAge)
            {
                heightCache[i] = null;
            }
        }
    }

    public static boolean isFinished()
    {
        return counter.get() == 0;
    }

    // Optimización: precalcular arrays de EnumFacing de forma más eficiente
    private static EnumFacing[][] makeEnumFacingArrays(boolean opposite)
    {
        int arraySize = 64;
        EnumFacing[][] facingArrays = new EnumFacing[arraySize][];
        EnumFacing[] facingValues = EnumFacing.VALUES;
        int facingCount = facingValues.length;

        for (int mask = 0; mask < arraySize; ++mask)
        {
            // Pre-calcular el tamaño de la lista para evitar redimensionado
            int bitCount = Integer.bitCount(mask);
            if (bitCount == 0)
            {
                facingArrays[mask] = new EnumFacing[0];
                continue;
            }

            EnumFacing[] facings = new EnumFacing[bitCount];
            int index = 0;

            for (int i = 0; i < facingCount; ++i)
            {
                EnumFacing facing = facingValues[i];
                EnumFacing targetFacing = opposite ? facing.getOpposite() : facing;
                int bit = 1 << targetFacing.ordinal();

                if ((mask & bit) != 0)
                {
                    facings[index++] = facing;
                }
            }

            facingArrays[mask] = facings;
        }

        return facingArrays;
    }

    public static EnumFacing[] getFacingsNotOpposite(int setDisabled)
    {
        int enabledMask = (~setDisabled) & MASK_FACINGS;
        return enumFacingOppositeArrays[enabledMask];
    }

    public static void reset()
    {
        worldLast = null;
        counter.set(0);

        // Limpiar cache completo
        for (int i = 0; i < CACHE_SIZE; i++)
        {
            heightCache[i] = null;
        }
        cacheHits = 0;
        cacheMisses = 0;
    }

    // Métodos de debug para monitorear performance del cache
    public static double getCacheHitRatio()
    {
        int total = cacheHits + cacheMisses;
        return total > 0 ? (double)cacheHits / total : 0.0;
    }

    public static void printCacheStats()
    {
        System.out.println("ChunkVisibility Cache - Hits: " + cacheHits +
                ", Misses: " + cacheMisses +
                ", Hit Rate: " + String.format("%.2f%%", getCacheHitRatio() * 100));
    }
}