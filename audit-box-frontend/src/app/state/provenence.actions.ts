import {Action} from '@ngrx/store'
import {Provenance, Provenance1} from '../entitiy/endpoint-entity';


export const ADD_PROVENENCE = '[PROVENENCE] Add';
export const REMOVE_PROVENENCE = '[PROVENENCE] Remove';

export class addProvenence implements Action {
  readonly type = ADD_PROVENENCE;

  constructor(public payload:Provenance) {
  }
}

export class removeProvenence implements Action {
  readonly type = REMOVE_PROVENENCE;

  constructor(public index:number) {
  }
}

export type Actions = addProvenence | removeProvenence
