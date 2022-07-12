package real

import complex.ComplexColumnVector
import complex.ComplexRowVector
import utils.R
import kotlin.math.sqrt
import utils.times

/**
 * Represents real row vector. It is also a [Matrix] with shape 1 * [length].
 *
 * @property length length of a vector.
 * @constructor Creates a new row vector. If [data] is not given, it will generate a zero vector.
 *
 * @param data must fit into the length of this vector.
 */
class RowVector(val length: Int, data: DoubleArray = DoubleArray(length){0.0}): Matrix(1, length, data) {
    init {
        if (data.size != length)
            throw IllegalArgumentException("RowVector.init: length of the data is not valid")
    }

    /**
     * data can be [LongArray].
     */
    constructor(length: Int, data: LongArray) : this(length, DoubleArray(length) { data[it].toDouble() })

    /**
     * data can be [FloatArray].
     */
    constructor(length: Int, data: FloatArray) : this(length, DoubleArray(length) { data[it].toDouble() })

    /**
     * data can be [IntArray].
     */
    constructor(length: Int, data: IntArray) : this(length, DoubleArray(length) { data[it].toDouble() })

    /**
     * It is possible to set a value using lambda function.
     */
    constructor(length: Int, lambda: (i: Int) -> Number) : this(length, DoubleArray(length) { lambda(it).toDouble() })

    constructor(data: DoubleArray) : this(data.size, data)

    constructor(data: LongArray) : this(data.size, data)

    constructor(data: FloatArray) : this(data.size, data)

    constructor(data: IntArray) : this(data.size, data)

    operator fun get(index: Int): Double {
        if (index < 0 || index >= length) {
            throw IllegalArgumentException("RowVector.get: Index out of bound")
        } else {
            return data[index]
        }
    }

    operator fun set(index: Int, value: Number) {
        if (index < 0 || index >= length) {
            throw IllegalArgumentException("RowVector.get: Index out of bound")
        } else {
            data[index] = value.toDouble()
        }
    }

    override operator fun unaryPlus() = this

    override operator fun unaryMinus(): RowVector {
        return RowVector(length, DoubleArray(length) {- data[it]})
    }

    operator fun plus(other: RowVector): RowVector {
        return if (length != other.length) {
            throw IllegalArgumentException("RowVector.plus: Two vectors should have the same size.")
        } else {
            val newData = DoubleArray(length) {
                data[it] + other.data[it]
            }
            RowVector(length, newData)
        }
    }

    operator fun minus(other: RowVector): RowVector {
        return if (length != other.length) {
            throw IllegalArgumentException("RowVector.minus: Two vectors should have the same size.")
        } else {
            val newData = DoubleArray(length) {
                data[it] - other.data[it]
            }
            RowVector(length, newData)
        }
    }

    override operator fun times(other: Matrix): RowVector {
        return if (length != other.rows) {
            throw IllegalArgumentException("RowVector.times: Illegal Matrix multiplication.")
        } else {
            val newData = DoubleArray(other.cols) {
                var sum = 0.0
                for (i in 0 until length) {
                    sum += this[i] * other[i, it]
                }
                sum
            }
            RowVector(other.cols, newData)
        }
    }

    override operator fun times(other: Number): RowVector {
        val newData = DoubleArray(length) {
            other.toDouble() * this[it]
        }
        return RowVector(length, newData)
    }

    override operator fun div(other: Number): RowVector {
        val newData = DoubleArray(length) {
            this[it] / other.toDouble()
        }
        return RowVector(length, newData)
    }

    /**
     * Same as [Matrix.transpose] but returns [ColumnVector].
     *
     * @return transposed vector.
     */
    override fun transpose(): ColumnVector {
        return ColumnVector(length, data)
    }

    /**
     * Get a subvector by slicing this vector.
     *
     * @param indexStart where slice starts.
     * @param indexEnd where slice ends. (not included)
     * @return sliced subvector.
     */
    fun getSubvector(indexStart: Int, indexEnd: Int): RowVector {
        return if (indexStart < 0 || indexStart >= indexEnd || indexEnd > length) {
            throw IllegalArgumentException("RowVector.Subvector: Index out of bound")
        } else {
            val newSize = indexEnd - indexStart
            val newData = DoubleArray(newSize) {
                this[indexStart + it]
            }
            RowVector(newSize, newData)
        }
    }

    /**
     * Set a subvector by substituting [other] to desired position.
     * The length of [other] must match the position you described.
     *
     * @param indexStart where substitution starts.
     * @param indexEnd where substitution ends. (not included)
     * @param other new subvector.
     */
    fun setSubvector(indexStart: Int, indexEnd: Int, other: RowVector) {
        val newSize = indexEnd - indexStart
        if (indexStart < 0 || indexStart >= indexEnd || indexEnd > length || newSize != other.length) {
            throw IllegalArgumentException("RowVector.Subvector: Index out of bound")
        } else {
            other.data.forEachIndexed { index, element ->
                this[indexStart + index] = element
            }
        }
    }

    /**
     * Same as [Matrix.eltwiseMul] but returns [RowVector].
     *
     * @param other must have the same length as this vector.
     * @return multiplied vector.
     */
    fun eltwiseMul(other: RowVector): RowVector {
        if (length != other.length)
            throw IllegalArgumentException("RowVector.eltwiseMul: Both operands must have the same size")
        return RowVector(length, DoubleArray(length) { this[it] * other[it] })
    }

    /**
     * Dot product.
     *
     * @param other must have the same length as this vector.
     * @return result of the product.
     */
    fun dotProduct(other: RowVector): Double {
        if (length != other.length)
            throw IllegalArgumentException("RowVector.dotProduct: Both operands must have the same size")
        var sum = 0.0
        for (i in 0 until length) {
            sum += this[i] * other[i]
        }
        return sum
    }

    /**
     * Dot product.
     *
     * @param other must have the same length as this vector.
     * @return result of the product.
     */
    fun dotProduct(other: ColumnVector): Double {
        if (length != other.length)
            throw IllegalArgumentException("RowVector.dotProduct: Both operands must have the same size")
        var sum = 0.0
        for (i in 0 until length) {
            sum += this[i] * other[i]
        }
        return sum
    }

    /**
     * Cross product. Available for 3-dimensional vectors only.
     *
     * @param other 3-dimensional vector.
     * @return result of the product.
     */
    fun crossProduct(other: RowVector): RowVector {
        if (length != 3 || other.length != 3)
            throw IllegalArgumentException("RowVector.dotProduct: Both operands must be 3 dimensional vectors")
        else {
            return RowVector(length, doubleArrayOf(
                this[1] * other[2] - this[2] * other[1],
                this[2] * other[0] - this[0] * other[2],
                this[0] * other[1] - this[1] * other[0]
            ))
        }
    }

    /**
     * Concatenate the copies of this vector [n] times.
     *
     * @param n
     * @return result of the concatenation.
     */
    fun replicate(n: Int): Matrix {
        if (n < 1) throw IllegalArgumentException("RowVector.replicate: length must be greater than 0.")
        return Matrix(n, this.length, DoubleArray(n * this.length) {
            val colIndex = it % this.length
            this[colIndex]
        })
    }

    /**
     * Same as [Tensor.map] but returns [RowVector].
     *
     * @param lambda mapping function.
     * @return a newly mapped vector.
     */
    override fun map(lambda: (e: Double) -> Number): RowVector {
        return RowVector(length, DoubleArray(length) {
            lambda(this[it]).toDouble()
        })
    }

    /**
     * Same as [Tensor.toComplex] but returns [ComplexColumnVector].
     *
     * @return converted [ComplexRowVector].
     */
    override fun toComplex(): ComplexRowVector {
        return ComplexRowVector(length, Array(length) {data[it].R})
    }

    /**
     * Same as [Tensor.copy] but returns [RowVector]
     *
     * @return a new [RowVector] instance with the same shape and data.
     */
    override fun copy() = RowVector(length, data.copyOf())

    /**
     * Project this vector onto [direction].
     *
     * @param direction direction vector.
     * @return result of the vector projection.
     */
    fun proj(direction: RowVector): RowVector {
        return (direction.dotProduct(this)) / direction.frobeniusNormSquared() * direction
    }

    /**
     * Normalize this vector.
     *
     * @return normalized vector.
     */
    fun normalize(): RowVector {
        return this / sqrt(this.frobeniusNormSquared())
    }
}
