/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.flowable.engine.impl.cmd;

import java.io.Serializable;

import org.flowable.engine.compatibility.Flowable5CompatibilityHandler;
import org.flowable.engine.delegate.event.FlowableEngineEventType;
import org.flowable.engine.delegate.event.impl.FlowableEventBuilder;
import org.flowable.engine.impl.interceptor.Command;
import org.flowable.engine.impl.interceptor.CommandContext;
import org.flowable.engine.impl.persistence.entity.AttachmentEntity;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.util.Flowable5Util;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class DeleteAttachmentCmd implements Command<Object>, Serializable {

    private static final long serialVersionUID = 1L;
    protected String attachmentId;

    public DeleteAttachmentCmd(String attachmentId) {
        this.attachmentId = attachmentId;
    }

    public Object execute(CommandContext commandContext) {
        AttachmentEntity attachment = commandContext.getAttachmentEntityManager().findById(attachmentId);

        String processInstanceId = attachment.getProcessInstanceId();
        String processDefinitionId = null;
        if (attachment.getProcessInstanceId() != null) {
            ExecutionEntity process = commandContext.getExecutionEntityManager().findById(processInstanceId);
            if (process != null) {
                processDefinitionId = process.getProcessDefinitionId();
                if (Flowable5Util.isFlowable5ProcessDefinitionId(commandContext, process.getProcessDefinitionId())) {
                    Flowable5CompatibilityHandler compatibilityHandler = Flowable5Util.getFlowable5CompatibilityHandler();
                    compatibilityHandler.deleteAttachment(attachmentId);
                    return null;
                }
            }
        }

        commandContext.getAttachmentEntityManager().delete(attachment, false);

        if (attachment.getContentId() != null) {
            commandContext.getByteArrayEntityManager().deleteByteArrayById(attachment.getContentId());
        }

        if (attachment.getTaskId() != null) {
            commandContext.getHistoryManager().createAttachmentComment(attachment.getTaskId(), attachment.getProcessInstanceId(), attachment.getName(), false);
        }

        if (commandContext.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
            commandContext.getProcessEngineConfiguration().getEventDispatcher()
                    .dispatchEvent(FlowableEventBuilder.createEntityEvent(FlowableEngineEventType.ENTITY_DELETED, attachment, processInstanceId, processInstanceId, processDefinitionId));
        }
        return null;
    }

}
