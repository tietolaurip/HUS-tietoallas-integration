---
-- #%L
-- ca
-- %%
-- Copyright (C) 2017 - 2018 Helsingin ja Uudenmaan sairaanhoitopiiri, Helsinki, Finland
-- %%
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
-- 
--      http://www.apache.org/licenses/LICENSE-2.0
-- 
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
-- #L%
---
update integration_status set time_column='ASA_UpdateTime', parameter_type='TIME_COMPARISATION' WHERE table_name='V_ASAClass'  and integration_name='ca'
UPDATE integration_status set time_column='V_AnesthesiaTechnique',parameter_type='TIME_COMPARISATION' WHERE table_name='AneT_UpdateTime'  and integration_name='ca'
UPDATE integration_status set time_column='BloG_UpdateTime',parameter_type='TIME_COMPARISATION' where table_name='V_BloodGroup'  and integration_name='ca'
UPDATE integration_status SET key_column='Cal_ID' WHERE table_name='V_Calendar' and integration_name='ca'
UPDATE integration_status SET time_column='CasP_UpdateTime',parameter_type='TIME_COMPARISATION' WHERE table_name='V_CasePhase' and integration_name='ca'
UPDATE integration_status SET time_column='CasT_UpdateTime',parameter_type='TIME_COMPARISATION' WHERE table_name='V_CaseType' and integration_name='ca'
UPDATE integration_status SET time_column='CodP_UpdateTime',parameter_type='TIME_COMPARISATION' WHERE table_name='V_CodePurpose' and integration_name='ca'
UPDATE integration_status SET key_column='Dut_ID' WHERE table_name='V_DurationType' and integration_name='ca'
UPDATE integration_status SET time_column='CasD_UpdateTime',parameter_type='TIME_COMPARISATION' WHERE table_name='V_F_CaseDetail' and integration_name='ca'
UPDATE integration_status SET key_column='Dut_ID' WHERE table_name='V_F_Duration' and integration_name='ca'
UPDATE integration_status SET time_column='LocU_UpdateTime',parameter_type='TIME_COMPARISATION' WHERE table_name='V_F_LocationUsage' and integration_name='ca'
UPDATE integration_status SET time_column='PhaCP_UpdateTime',parameter_type='TIME_COMPARISATION' WHERE table_name='V_F_PharmaConsumptionPure' and integration_name='ca'
UPDATE integration_status SET time_column='RecC_UpdateTime',parameter_type='TIME_COMPARISATION' WHERE table_name='V_F_RecCode' and integration_name='ca'
UPDATE integration_status SET time_column='Tim_UpdateTime',parameter_type='TIME_COMPARISATION' WHERE table_name='V_F_Time' and integration_name='ca'
UPDATE integration_status SET time_column='Job_UpdateTime',parameter_type='TIME_COMPARISATION' WHERE table_name='V_JobRole' and integration_name='ca'
UPDATE integration_status SET time_column='Loc_UpdateTime',parameter_type='TIME_COMPARISATION' WHERE table_name='V_Location' and integration_name='ca'
UPDATE integration_status SET time_column='CasM_UpdateTime',parameter_type='TIME_COMPARISATION'WHERE table_name='V_MainCase' and integration_name='ca'
UPDATE integration_status SET time_column='OVC_UpdateTime',parameter_type='TIME_COMPARISATION'WHERE table_name='V_OrdinalVariableChoices' and integration_name='ca'
UPDATE integration_status SET time_column='Pat_UpdateTime',parameter_type='TIME_COMPARISATION' WHERE table_name='V_Patient' and integration_name='ca'
UPDATE integration_status SET key_column='PatC_ID' WHERE table_name='V_PatientCaseAge' and integration_name='ca'
UPDATE integration_status SET table_name='PatCla_UpdateTime',parameter_type='TIME_COMPARISATION' WHERE table_name='V_PatientClass' and integration_name='ca'
UPDATE integration_status SET time_column='PatG_UpdateTime',parameter_type='TIME_COMPARISATION' WHERE table_name='V_PatientGender' and integration_name='ca'
UPDATE integration_status SET time_column='PhaG_UpdateTime',parameter_type='TIME_COMPARISATION'WHERE table_name='V_PharmaGroup' and integration_name='ca'
UPDATE integration_status SET time_column='Pha_UpdateTime',parameter_type='TIME_COMPARISATION' WHERE table_name='V_PharmaProduct' and integration_name='ca'
UPDATE integration_status SET time_column='PDL_UpdateTime',parameter_type='TIME_COMPARISATION' WHERE table_name='V_ProcDiagList' and integration_name='ca'
UPDATE integration_status SET key_column='Rou_DocID',parameter_type='STATUSDB_TABLE_LOOKUP' WHERE table_name='V_Route' and integration_name='ca'
UPDATE integration_status SET time_column='Spec_UpdateTime',parameter_type='TIME_COMPARISATION' WHERE table_name='V_Speciality' and integration_name='ca'
UPDATE integration_status SET time_column='Sta_UpdateTime',parameter_type='TIME_COMPARISATION' WHERE table_name='V_Staff' and integration_name='ca'
UPDATE integration_status SET time_column='SurS_UpdateTime',parameter_type='TIME_COMPARISATION' WHERE table_name='V_SurgicalService' and integration_name='ca'
UPDATE integration_status SET key_column='TimT_ID' WHERE table_name='V_TimeType' and integration_name='ca'
UPDATE integration_status SET key_column='Uni_ID', parameter_type='STATUSDB_TABLE_LOOKUP' WHERE table_name='V_Unit' and integration_name='ca'
UPDATE integration_status SET time_column='WoundCla_UpdateTime', parameter_type='TIME_COMPARISATION' WHERE table_name='V_WoundClass' and integration_name='ca'
