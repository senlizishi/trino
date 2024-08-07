/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.trino.operator;

import io.trino.spi.Page;
import jakarta.annotation.Nullable;

import static com.google.common.base.Preconditions.checkState;
import static io.trino.operator.WorkProcessor.ProcessState.finished;
import static io.trino.operator.WorkProcessor.ProcessState.ofResult;
import static io.trino.operator.WorkProcessor.ProcessState.yielded;
import static java.util.Objects.requireNonNull;

/**
 * Provides a bridge between classic {@link Operator} push input model
 * and {@link WorkProcessorOperator} pull model.
 */
public class PageBuffer
{
    @Nullable
    private Page page;
    private boolean finished;

    public WorkProcessor<Page> pages()
    {
        return WorkProcessor.create(() -> {
            if (isFinished() && isEmpty()) {
                return finished();
            }

            if (!isEmpty()) {
                Page result = page;
                page = null;
                return ofResult(result);
            }

            return yielded();
        });
    }

    public boolean isEmpty()
    {
        return page == null;
    }

    public boolean isFinished()
    {
        return finished;
    }

    public void add(Page page)
    {
        checkState(isEmpty(), "page buffer is not empty");
        checkState(!isFinished(), "page buffer is finished");
        requireNonNull(page, "page is null");

        if (page.getPositionCount() == 0) {
            return;
        }

        this.page = page;
    }

    public void finish()
    {
        finished = true;
    }
}
