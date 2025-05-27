import { AbstractEntity } from './abstract-dto.model';
import { StaffingProcessDTO } from './staffing-process-dto.model';

export interface ClientDTO extends AbstractEntity {
  clientName?: string;
  clientEmail?: string;
  staffingProcesses?: StaffingProcessDTO[];
  contactPersonName?: string;
  contactPersonEmail?: string;
  contactPersonPhone?: string;
}
