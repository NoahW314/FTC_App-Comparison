/*
Copyright (c) 2018 Robert Atkinson

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Robert Atkinson nor the names of his contributors may be used to
endorse or promote products derived from this software without specific prior
written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESSFOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.firstinspires.ftc.robotcore.internal.system;

import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;

/**
 * {@link CloseableOnFinalize} instances will be (internally) closed on finalization, if they haven't
 * already been closed beforehand. In this way, underlying resources can be guaranteed to be
 * reclaimed.
 */
@SuppressWarnings("WeakerAccess")
public abstract class CloseableOnFinalize<ParentType extends RefCounted> extends Closeable implements Finalizable
    {
    //----------------------------------------------------------------------------------------------
    // State
    //----------------------------------------------------------------------------------------------

    protected Finalizer     finalizer = Finalizer.forTarget(this);
    protected boolean       inFinalize = false;
    protected ParentType    parent = null;
    protected boolean       ownParentRef = false;

    //----------------------------------------------------------------------------------------------
    // Construction
    //----------------------------------------------------------------------------------------------

    protected CloseableOnFinalize()
        {
        super();
        }

    protected void setParent(@Nullable ParentType newParent)
        {
        synchronized (lock)
            {
            // AddRefs before releases: What if newParent is same as old?
            if (parent != newParent)
                {
                if (this.parent != null)
                    {
                    this.parent.releaseRef();
                    ownParentRef = false;
                    }
                if (newParent != null)
                    {
                    newParent.addRef();
                    ownParentRef = true;
                    }
                this.parent = newParent;
                }
            }
        }

    /**
     * Returns the parent of this object.
     */
    protected ParentType getParent()
        {
        return parent;
        }

    public void doFinalize()
        {
        synchronized (lock)
            {
            inFinalize = true;
            try {
                close();
                }
            finally
                {
                inFinalize = false;
                }
            }
        }

    protected void suppressFinalize()
        {
        synchronized (lock)
            {
            if (this.finalizer != null)
                {
                this.finalizer.dispose();
                this.finalizer = null;
                }
            }
        }

    protected void preClose()
        {
        suppressFinalize();
        super.preClose();
        }

    @CallSuper @Override protected void doClose()
        {
        if (ownParentRef)
            {
            parent.releaseRef();
            ownParentRef = false;
            }
        super.doClose();
        }
    }