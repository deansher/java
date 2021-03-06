/* Copyright 2020 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
=======================================================================*/
package org.tensorflow.framework.initializers;

import org.tensorflow.Operand;
import org.tensorflow.framework.utils.ShapeUtils;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.op.Ops;
import org.tensorflow.types.TInt64;
import org.tensorflow.types.family.TFloating;

/**
 * Initializer that generates the identity matrix.
 *
 * <p>Only usable for generating 2D matrices.
 *
 * <p>Examples:
 *
 * <pre>
 *     Identity&lt;TFloat32&gt; initializer =
 *             new org.tensorflow.framework.initializers.Identity&lt;&gt;(tf);
 *     Operand&lt;TFloat32&gt; values =
 *             initializer.call(tf.constant(Shape.of(2,2)), TFloat32.class);
 * </pre>
 *
 * @param <T> The TType for the call operation
 */
public class Identity<T extends TFloating> extends BaseInitializer<T> {
  public static final double GAIN_DEFAULT = 1.0;

  private final double gain;

  /**
   * Creates an Initializer that generates the identity matrix.
   *
   * @param tf the TensorFlow Ops
   */
  public Identity(Ops tf) {
    super(tf);
    this.gain = GAIN_DEFAULT;
  }

  /**
   * Creates an Initializer that generates the identity matrix.
   *
   * @param tf the TensorFlow Ops
   * @param gain the gain to be applied to the Identity Matrix
   */
  public Identity(Ops tf, double gain) {
    super(tf);
    this.gain = gain;
  }

  /** {@inheritDoc} */
  @Override
  public Operand<T> call(Operand<TInt64> dims, Class<T> type) {
    Shape shape = ShapeUtils.toShape(tf.scope(), dims);
    if (shape.numDimensions() != 2) {
      throw new IllegalArgumentException("2D matrix required, got " + shape.numDimensions());
    }
    boolean isSquare = shape.size(0) == shape.size(1);
    long diagSize = Math.min(shape.size(0), shape.size(1));
    Shape diagShape = Shape.of(diagSize);

    Operand<T> op;
    Operand<T> zero = tf.dtypes.cast(tf.constant(0), type);
    Operand<T> diagOnes =
        tf.fill(tf.constant(diagShape.asArray()), tf.dtypes.cast(tf.constant(1.0), type));
    if (isSquare) {
      op =
          tf.linalg.matrixDiag(
              diagOnes,
              tf.constant(0), // don't cast here, expecting TInt32
              tf.constant((int) shape.size(0)),
              tf.constant((int) shape.size(1)),
              zero);
    } else {
      Operand<T> zeroMatrix = tf.zeros(dims, type);
      op = tf.linalg.matrixSetDiag(zeroMatrix, diagOnes, tf.constant(0));
    }

    return tf.math.mul(op, tf.dtypes.cast(tf.constant(gain), type));
  }
}
